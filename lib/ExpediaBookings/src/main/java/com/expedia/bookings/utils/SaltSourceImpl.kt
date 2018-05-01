package com.expedia.bookings.utils

import java.util.Random

class SaltSourceImpl : SaltSource {
    private val alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    override fun salt(length: Int): String {
        val salt = StringBuilder()
        for (i in 0 until length) {
            salt.append(alphaNumericChars[Random().nextInt(alphaNumericChars.length)])
        }
        return salt.toString()
    }
}
