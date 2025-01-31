package com.voborodin.f2dice.types

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IBTController {
    val scannedDevices: StateFlow<List<BTDevice>>
    val pairedDevices: StateFlow<List<BTDevice>>
    val isConnected: StateFlow<Boolean>
    val errors: SharedFlow<String>
    fun startDiscovery()
    fun stopDiscovery()

    fun startBTServer(): Flow<ConnectionResult>

    fun connectToDevice(device: BTDevice): Flow<ConnectionResult>

    fun closeConnection()

    fun release()

    suspend fun trySendMsg(msg: String): BTMsg?
}