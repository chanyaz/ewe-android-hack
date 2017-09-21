package com.expedia.bookings.shared

import org.joda.time.LocalDate

interface CalendarListener {
    fun datesUpdated(startDate: LocalDate?, endDate: LocalDate?)
}