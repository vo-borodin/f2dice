package com.voborodin.f2dice.utils

import com.voborodin.f2dice.R
import com.voborodin.f2dice.types.BTDevice
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun setImage(rand : Int): Int {
    val res = when(rand){
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
    return res
}

fun parseBTDevice(json: String): BTDevice? {
    if (json.isBlank()) {
        return null
    }
    val jsonObj = JSONObject(json)
    val address = jsonObj.getString("address")
    val name = jsonObj.getString("name")

    return BTDevice(name, address)
}

fun dumpBTDevice(device: BTDevice?): String {
    if (device == null || device.address.isBlank()) {
        return ""
    }
    return "{ name: \"${device.name}\", address: \"${device.address}\" }"
}
