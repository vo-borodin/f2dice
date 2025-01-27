package com.voborodin.f2dice.viewModel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import com.voborodin.f2dice.R
import com.voborodin.f2dice.utils.DiceDataStore
import com.voborodin.f2dice.utils.setImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class FDiceViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    val developerURI: String = "https://github.com/vo-borodin"
    val appURI: String = "https://github.com/vo-borodin/f2dice"
    val issuesURI: String = "https://github.com/vo-borodin/f2dice/issues"

    private val diceDataStore = DiceDataStore.getInstance(application)
    var appTheme = diceDataStore.getAppTheme().asLiveData()
    var gameMode = diceDataStore.getGameMode().asLiveData()

    private val _eventGameStart = MutableLiveData<Boolean>()
    val eventGameStart: LiveData<Boolean>
        get() = _eventGameStart

    private val _dice1 = MutableLiveData<Int>()
    val dice1: LiveData<Int>
        get() = _dice1

    private val _dice2 = MutableLiveData<Int>()
    val dice2: LiveData<Int>
        get() = _dice2

    private val _forgery = MutableLiveData<Int?>()
    val forgery: LiveData<Int?>
        get() = _forgery

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    init {
        _eventGameStart.value = false
        _dice1.value = R.drawable.empty_dice
        _dice2.value = R.drawable.empty_dice
        _forgery.value = null
        _result.value = ""
    }

    fun setGameMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setGameMode(mode)
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            diceDataStore.setAppTheme(theme)
        }
    }

    fun resetData() {
        _dice1.value = R.drawable.empty_dice
        _dice2.value = R.drawable.empty_dice
        _result.value = ""
    }

    fun onGameStart() {
        _eventGameStart.value = true
    }

    fun onGameStartComplete() {
        _eventGameStart.value = false
    }

    private fun provideHapticFeedback() {
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
        return pairs[Random.nextInt(0, pairs.count() - 1)]
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
}