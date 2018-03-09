package com.expedia.bookings.itin.utils

interface StringSource {
    fun fetch(stringResource: Int): String
    fun fetch(stringResource: Int, map: Map<String, String>): String
}
