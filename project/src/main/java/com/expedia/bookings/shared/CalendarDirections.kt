package com.expedia.bookings.shared

import org.joda.time.LocalDate

interface CalendarDirections {
    fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String
    fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String
    fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String
    fun getToolTipInstructions(end: LocalDate?) : String
}