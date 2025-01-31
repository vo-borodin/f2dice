package com.voborodin.f2dice.types

interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    class ConnectionError(val msg: String) : ConnectionResult

    data class TransferSucceeded(val msg: BTMsg) : ConnectionResult
}