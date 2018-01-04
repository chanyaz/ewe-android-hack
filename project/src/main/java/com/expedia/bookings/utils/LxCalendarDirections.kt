package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarDirections
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getDateAccessibilityText
import org.joda.time.LocalDate

class LxCalendarDirections(private val context: Context) : CalendarDirections {
    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getString(R.string.select_lx_search_dates)
        }
        return LocaleBasedDateFormatUtils.localDateToMMMd(start!!)
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        if (forContentDescription) {
            return getDateAccessibilityText(context, context.getString(R.string.select_start_date),
                    LocaleBasedDateFormatUtils.localDateToMMMMd(start))
        } else {
            return LocaleBasedDateFormatUtils.localDateToMMMMd(start)
        }
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        if (forContentDescription) {
            return getDateAccessibilityText(context, context.getString(R.string.select_start_date),
                    LocaleBasedDateFormatUtils.localDateToMMMMd(start!!))
        } else {
            return LocaleBasedDateFormatUtils.localDateToMMMMd(start!!)
        }
    }

    override fun getToolTipInstructions(end: LocalDate?): String {
        return context.getString(R.string.calendar_drag_to_modify)
    }
}