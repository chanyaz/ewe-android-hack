package com.expedia.bookings.shared

import org.joda.time.LocalDate

interface CalendarRules {
    fun getMaxDateRange(): Int
    fun getFirstAvailableDate(): LocalDate
    fun getMaxSearchDurationDays(): Int
    fun sameStartAndEndDateAllowed(): Boolean
    fun isStartDateOnlyAllowed(): Boolean
}
