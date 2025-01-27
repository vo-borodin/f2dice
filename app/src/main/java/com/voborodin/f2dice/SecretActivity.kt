package com.voborodin.f2dice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.voborodin.f2dice.databinding.ActivitySecretBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecretActivity : ComponentActivity() {
    private lateinit var binding: ActivitySecretBinding

    private val btManager by lazy {
        this.getSystemService(BluetoothManager::class.java)
    }
    private val btAdapter by lazy {
        btManager?.adapter
    }

    private val isBTEnabled: Boolean
        get() = btAdapter?.isEnabled == true

    private val enableBluetoothDevice = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {/*no needed*/ }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        var canEnableBluetooth = true

        requiredPermissions.forEach {
            if (perms[it] == false) canEnableBluetooth = false
        }

        if (canEnableBluetooth && !isBTEnabled) {
            enableBluetoothDevice.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }

    private val requiredPermissions = ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_secret)

        if (!hasBTPermission()) {
            permissionLauncher.launch(requiredPermissions)
        } else if (!isBTEnabled) enableBluetoothDevice.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )

    }

    private fun hasBTPermission(): Boolean {
        var hasPermission = true
        requiredPermissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false
            }
        }
        return hasPermission
    }
}
