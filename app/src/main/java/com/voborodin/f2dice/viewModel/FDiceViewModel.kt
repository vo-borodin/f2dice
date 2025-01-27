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

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result


    init {
        _eventGameStart.value = false
        _dice1.value = R.drawable.empty_dice
        _dice2.value = R.drawable.empty_dice
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
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // Vibrate method for below API Level 26
                vibrator.vibrate(100)
            }
        }
    }

    fun rollBoardTwo() {
        provideHapticFeedback()
        val randomIntOne: Int = Random.nextInt(6) + 1
        val randomIntTwo: Int = Random.nextInt(6) + 1
        _dice1.value = setImage(randomIntOne)
        _dice2.value = setImage(randomIntTwo)
        _result.value = (randomIntOne + randomIntTwo).toString()
    }
}