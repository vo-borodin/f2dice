package com.voborodin.f2dice.msg

import com.voborodin.f2dice.types.BTMsg

fun String.toBTMsg(isFromLocalUser: Boolean): BTMsg {
    val name = substringBeforeLast("#")
    val message = substringAfter("#")
    return BTMsg(
        senderName = name, msg = message, isFromLocalUser = isFromLocalUser
    )
}

fun BTMsg.toByteArray(): ByteArray {
    return "$senderName#$msg".encodeToByteArray()
}