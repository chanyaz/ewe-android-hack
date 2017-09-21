package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarRules
import org.joda.time.LocalDate

class HotelCalendarRules(private val context: Context) : CalendarRules {
    private val res by lazy { context.resources }

    override fun getMaxDateRange(): Int {
        return res.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only)
    }

    override fun getFirstAvailableDate(): LocalDate {
        return LocalDate.now()
    }

    override fun getMaxSearchDurationDays(): Int {
        return res.getInteger(R.integer.calendar_max_days_hotel_stay)
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false
    }
}