package com.expedia.bookings.utils

import com.expedia.bookings.data.HolidayCalendarResponse
import org.joda.time.LocalDate

fun HolidayCalendarResponse.toListOfDates(): List<LocalDate> {
    return holidays.asSequence()
            .map { LocalDate(it.holidayDateString) }
            .toList()
}
