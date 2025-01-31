package com.voborodin.f2dice.msg

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.voborodin.f2dice.types.BTDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBTDevice() = BTDevice(
    name = this.name, address = this.address
)