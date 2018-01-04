package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarDirections
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class RailCalendarDirections(private val context: Context, private val roundTrip: Boolean) :
        CalendarDirections {
    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return getCalendarDateLabel()
        } else if (end == null && roundTrip) {
            return Phrase.from(context.resources, R.string.select_return_date_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(start!!))
                    .format().toString()
        }
        return DateFormatUtils.formatRailDateRange(context, start, end)
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        return "" //no op, rail doesn't update until time is selected.
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        return "" //no op, rail doesn't update until time is selected.
    }

    override fun getToolTipInstructions(end: LocalDate?): String {
        if (roundTrip && end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    private fun getCalendarDateLabel() : String {
        val resId = if (roundTrip) R.string.select_dates else R.string.select_departure_date
        return context.getString(resId)
    }
}