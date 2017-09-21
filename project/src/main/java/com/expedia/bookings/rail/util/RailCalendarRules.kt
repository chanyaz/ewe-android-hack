package com.expedia.bookings.rail.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarRules
import org.joda.time.LocalDate

class RailCalendarRules(private val context: Context, private val roundTrip: Boolean) : CalendarRules {
    private val res by lazy { context.resources }

    override fun getMaxDateRange(): Int {
        return res.getInteger(R.integer.calendar_max_days_rail_search)
    }

    override fun getFirstAvailableDate(): LocalDate {
        return LocalDate.now().plusDays(1)
    }

    override fun getMaxSearchDurationDays(): Int {
        return if (roundTrip) context.resources.getInteger(R.integer.calendar_max_days_rail_return) else 0
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return !roundTrip
    }
}