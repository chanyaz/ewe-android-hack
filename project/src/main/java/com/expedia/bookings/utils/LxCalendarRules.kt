package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarRules
import org.joda.time.LocalDate

class LxCalendarRules(private val context: Context) : CalendarRules {
    private val res by lazy { context.resources }

    override fun getMaxDateRange(): Int {
        return res.getInteger(R.integer.calendar_max_days_lx_search)
    }

    override fun getFirstAvailableDate(): LocalDate {
        return LocalDate.now()
    }

    override fun getMaxSearchDurationDays(): Int {
        return if (isLXMultipleDatesSearchEnabled()) {
            Constants.LX_CALENDAR_MAX_DATE_SELECTION
        } else {
            res.getInteger(R.integer.calendar_max_selection_date_range_lx)
        }
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return isLXMultipleDatesSearchEnabled()
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return !isLXMultipleDatesSearchEnabled()
    }
}
