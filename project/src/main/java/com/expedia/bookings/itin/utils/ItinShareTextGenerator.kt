package com.expedia.bookings.itin.utils

interface ItinShareTextGenerator {
    fun getEmailSubject(): String
    fun getEmailBody(): String
    fun getSmsBody(): String
    fun getLOBTypeString(): String
}
