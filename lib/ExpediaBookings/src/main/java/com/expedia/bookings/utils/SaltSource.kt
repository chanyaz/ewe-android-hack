package com.expedia.bookings.utils

interface SaltSource {
    fun salt(length: Int): String
}
