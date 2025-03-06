package com.voborodin.f2dice.viewModel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.*
import com.voborodin.f2dice.F2DiceApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import com.voborodin.f2dice.R
import com.voborodin.f2dice.msg.BTController
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.types.BTUiState
import com.voborodin.f2dice.types.ConnectionResult
import com.voborodin.f2dice.types.Role
import com.voborodin.f2dice.utils.F2DiceDataStore
import com.voborodin.f2dice.utils.setImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class F2DiceViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {
    private val btController: BTController = F2DiceApplication.btController
    private val _state = MutableStateFlow(BTUiState())
    val state = combine(
        btController.scannedDevices, btController.pairedDevices, _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if (state.isConnected) state.messages else emptyList()
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), _state.value
    )

    private var deviceConnectionJob: Job? = null

    val developerURI: String = "https://github.com/vo-borodin"
    val appURI: String = "https://github.com/vo-borodin/f2dice"
    val issuesURI: String = "https://github.com/vo-borodin/f2dice/issues"

    private val diceDataStore = F2DiceDataStore.getInstance(application)
    var appTheme = diceDataStore.getAppTheme().asLiveData()
    var connectedDevice = diceDataStore.getConnectedDevice().asLiveData()
    var role = diceDataStore.getRole().asLiveData()
    var pinHash = diceDataStore.getPinHash().asLiveData()

    private val _dice1 = MutableLiveData<Int>()
    val dice1: LiveData<Int>
        get() = _dice1

    private val _dice2 = MutableLiveData<Int>()
    val dice2: LiveData<Int>
        get() = _dice2

    private val _forgery = MutableLiveData<Int?>()
    var forgery: MutableLiveData<Int?>
        get() = _forgery
        set(f) {
            _forgery.value = f.value
        }

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    init {
        _dice1.value = R.drawable.empty_dice
        _dice2.value = R.drawable.empty_dice
        _forgery.value = null
        _result.value = ""

        btController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        btController.errors.onEach { errMsg ->
            _state.update { it.copy(errorMsg = errMsg) }
        }.launchIn(viewModelScope)
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setAppTheme(theme)
        }
    }

    fun setConnectedDevice(device: BTDevice?) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setConnectedDevice(device)
        }
    }

    fun setRole(role: Role?) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setRole(role)
        }
    }

    fun setPinHash(pinHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setPinHash(pinHash)
        }
    }

    fun resetData() {
        _dice1.value = R.drawable.empty_dice
        _dice2.value = R.drawable.empty_dice
        _result.value = ""
    }

    fun provideHapticFeedback() {
        val vibrator =
            getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // New vibrate method for API Level 26 or higher
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        100, VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // Vibrate method for below API Level 26
                vibrator.vibrate(100)
            }
        }
    }

    private fun randomForgedRolling(predict: Int): Pair<Int, Int> {
        val pairs = (1..predict).mapNotNull {
            if ((predict - it) <= 6 && (predict - it) >= 1) {
                Pair(it, predict - it)
            } else {
                null
            }
        }
        return pairs[Random.nextInt(0, pairs.count())]
    }

    fun rollBoard() {
        val randomIntOne: Int
        val randomIntTwo: Int
        provideHapticFeedback()

        if (forgery.value == null || forgery.value!! < 2 || forgery.value!! > 12) {
            randomIntOne = Random.nextInt(6) + 1
            randomIntTwo = Random.nextInt(6) + 1
        } else {
            val pair = randomForgedRolling(forgery.value!!)
            randomIntOne = pair.first
            randomIntTwo = pair.second
        }

        _dice1.value = setImage(randomIntOne)
        _dice2.value = setImage(randomIntTwo)
        _forgery.value = null
        _result.value = (randomIntOne + randomIntTwo).toString()
    }

    fun startScan() {
        btController.startDiscovery()
    }

    fun stopScan() {
        btController.stopDiscovery()
    }

    fun connectToDevice(device: BTDevice) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = btController.connectToDevice(device).listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        btController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false, isConnected = false
            )
        }
    }

    fun waitForIncomingConnection() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = btController.startBTServer().listen()
    }

    fun sendForgery() {
        if (role.value == Role.Master && forgery.value != null) {
            sendMsg(forgery.value.toString())
        }
    }

    private fun sendMsg(msg: String) {
        viewModelScope.launch {
            val btMsg = btController.trySendMsg(msg)
            if (btMsg != null) _state.update {
                it.copy(
                    messages = it.messages + btMsg
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        btController.release()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true, isConnecting = false, errorMsg = null
                        )
                    }
                }

                is ConnectionResult.ConnectionError -> {
                    _state.update {
                        it.copy(
                            isConnected = false, isConnecting = false, errorMsg = result.msg
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    _state.update {
                        it.copy(
                            messages = it.messages + result.msg
                        )
                    }
                    if (!result.msg.isFromLocalUser) {
                        forgery.value = result.msg.msg.toInt()
                    }
                }
            }
        }.catch {
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }
}