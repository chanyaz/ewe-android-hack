package com.expedia.bookings.itin.utils

interface ShareItinTextCreator {
    fun getEmailSubject(): String
    fun getEmailBody(): String
    fun getSmsBody(): String
}
