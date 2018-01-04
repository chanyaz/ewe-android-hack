package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarDirections
import com.expedia.bookings.shared.util.CalendarDateFormatter
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getDateAccessibilityText
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getStartDashEndDateWithDayString
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getStartToEndDateWithDayString
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class FlightCalendarDirections(private val context: Context, private val roundTrip: Boolean) :
        CalendarDirections {
    override fun getToolTipInstructions(end: LocalDate?) : String {
        if (roundTrip && end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date)
        } else if (end == null) {
            return getNoEndDateText(start, false)
        }
        return getCompleteDateText(start!!, end, false)
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        if (roundTrip) {
            val dateString = Phrase.from(context.resources, R.string.select_return_date_TEMPLATE)
                    .put("startdate", getFormattedDate(start))
                    .format().toString()
            if (forContentDescription) {
                return CalendarDateFormatter.getDateAccessibilityText(context,
                        context.getString(R.string.select_dates), dateString)
            }
            return dateString
        } else {
            val dateString = Phrase.from(context.resources, R.string.calendar_instructions_date_range_flight_one_way_TEMPLATE)
                    .put("startdate", getFormattedDate(start))
                    .format().toString()
            if (forContentDescription) {
                return CalendarDateFormatter.getDateAccessibilityText(context,
                        context.getString(R.string.select_dates), dateString)
            }
            return dateString
        }
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        if (forContentDescription) {
            val formattedDate = getStartToEndDateWithDayString(context, start, end)
            return getDateAccessibilityText(context, context.getString(R.string.select_dates), formattedDate)
        }
        return getStartDashEndDateWithDayString(context, start, end)
    }

    private fun getFormattedDate(date: LocalDate?): String? {
        return DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(date)
    }
}