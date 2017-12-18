package com.expedia.bookings.tracking

interface TimeSource {
    fun now(): Long
}
