package com.voborodin.f2dice.types

data class BTMsg(
    val msg: String,
    val senderName: String,
    val isFromLocalUser: Boolean
)