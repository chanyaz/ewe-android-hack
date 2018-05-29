package com.expedia.bookings.utils.validation

import com.expedia.bookings.shared.CalendarRules
import org.joda.time.Days
import org.joda.time.LocalDate

/** This validator view max date range and max duration as inclusive
 *
 * Assuming first available date is today.
 *
 * ex1: if max date range is 1, today and tomorrow are within range, not two days from today
 *
 * ex2: if max duration is 1, start and end date can be today and tomorrow, not today and two days from today
*/
class CalendarRulesDateValidator(private val calendarRules: CalendarRules, private val allowEndDateEqualLastDatePlusOne: Boolean) {

    private val firstAvailableDate = calendarRules.getFirstAvailableDate()
    private val lastAvailableDate = firstAvailableDate.plusDays(calendarRules.getMaxDateRange())
    private val lastAvailableDatePlusOne = firstAvailableDate.plusDays(calendarRules.getMaxDateRange() + 1)

    fun validateStartEndDate(startDate: LocalDate?, endDate: LocalDate?): Boolean {
        if (startDate == null) {
            return false
        }
        if (endDate == null) {
            if (!calendarRules.isStartDateOnlyAllowed()) {
                return false
            }
            return validateStartDateWithInRange(startDate)
        } else {

            val startDateWithinRange = validateStartDateWithInRange(startDate)
            val endDateWithinRange = validateEndDateWithInRange(endDate)
            val dateWithinDuration = validateDatesWithInDuration(startDate, endDate)
            val startBeforeEndDate = validateStartBeforeEndDate(startDate, endDate)

            return startDateWithinRange && endDateWithinRange && dateWithinDuration && startBeforeEndDate
        }
    }

    private fun validateStartDateWithInRange(startDate: LocalDate): Boolean {
        var validateEqualLastDate = true
        if (startDate.isEqual(lastAvailableDate)) {
            validateEqualLastDate = calendarRules.sameStartAndEndDateAllowed() || calendarRules.isStartDateOnlyAllowed() || allowEndDateEqualLastDatePlusOne
        }

        return validateEqualLastDate && !startDate.isBefore(firstAvailableDate) && !startDate.isAfter(lastAvailableDate)
    }

    private fun validateEndDateWithInRange(endDate: LocalDate): Boolean {
        var validateEqualFirstDate = true
        if (endDate.isEqual(firstAvailableDate)) {
            validateEqualFirstDate = calendarRules.sameStartAndEndDateAllowed()
        }

        var validateAfterLastDate = true
        if (endDate.isAfter(lastAvailableDate)) {
            if (endDate.isEqual(lastAvailableDatePlusOne)) {
                validateEqualFirstDate = allowEndDateEqualLastDatePlusOne
            } else {
                validateAfterLastDate = false
            }
        }

        return validateEqualFirstDate && !endDate.isBefore(firstAvailableDate) && validateAfterLastDate
    }

    private fun validateDatesWithInDuration(startDate: LocalDate, endDate: LocalDate): Boolean {
        return Days.daysBetween(startDate, endDate).days <= calendarRules.getMaxSearchDurationDays()
    }

    private fun validateStartBeforeEndDate(startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.isBefore(endDate)) {
            return true
        }
        if (startDate.isEqual(endDate)) {
            return calendarRules.sameStartAndEndDateAllowed()
        }
        return false
    }
}
