package com.expedia.bookings.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

interface DateTimeSource {
    fun now(zone: DateTimeZone): DateTime
}
