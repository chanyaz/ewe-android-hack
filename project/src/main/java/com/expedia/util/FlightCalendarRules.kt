package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarRules
import org.joda.time.LocalDate

class FlightCalendarRules(context: Context, private val roundTrip: Boolean) : CalendarRules {
    private val res by lazy { context.resources }

    override fun getMaxDateRange(): Int {
        return res.getInteger(R.integer.calendar_max_days_flight_search)
    }

    override fun getFirstAvailableDate(): LocalDate {
        return LocalDate.now()
    }

    override fun getMaxSearchDurationDays(): Int {
        return if (roundTrip) res.getInteger(R.integer.calendar_max_days_flight_search) else 0
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return true
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return true
    }
}