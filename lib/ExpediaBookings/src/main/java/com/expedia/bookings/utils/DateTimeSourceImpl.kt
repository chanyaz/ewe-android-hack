package com.expedia.bookings.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class DateTimeSourceImpl : DateTimeSource {
    override fun now(zone: DateTimeZone): DateTime {
        return DateTime.now(zone)
    }
}
