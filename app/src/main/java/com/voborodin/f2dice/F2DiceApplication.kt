package com.voborodin.f2dice

import android.app.Application
import com.voborodin.f2dice.msg.BTController
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class F2DiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        btController = BTController(this)
    }

    companion object {
        lateinit var btController: BTController
            private set
    }
}
