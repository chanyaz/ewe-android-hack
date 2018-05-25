package com.expedia.bookings.utils

import com.expedia.bookings.data.HolidayCalendarResponse
import org.joda.time.LocalDate

fun HolidayCalendarResponse.toListOfDates(): List<LocalDate> {
    return holidays.asSequence()
            .map { LocalDate(it.holidayDateString) }
            .toList()
}

fun HolidayCalendarResponse.toMapOfDatesToNames(): Map<LocalDate, String> {
    val holidayInfoHashMap = LinkedHashMap<LocalDate, String>()
    holidays.forEach { holidayInfoHashMap[LocalDate(it.holidayDateString)] = it.holidayName }
    return holidayInfoHashMap
}
