package com.expedia.bookings.test

import com.expedia.bookings.tracking.TimeSource

class MockTimeSource : TimeSource {
    var timeNow: Long = 0
    override fun now(): Long {
        return timeNow
    }
}
