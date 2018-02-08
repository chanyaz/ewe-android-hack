package com.expedia.bookings.services

interface NonFatalLoggerInterface {
    fun logException(e: Exception)
}
