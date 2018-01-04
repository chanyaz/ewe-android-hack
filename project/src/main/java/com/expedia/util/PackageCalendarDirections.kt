package com.expedia.util

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarDirections
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getDateAccessibilityText
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getStartDashEndDateWithDayString
import com.expedia.bookings.shared.util.CalendarDateFormatter.Companion.getStartToEndDateWithDayString
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class PackageCalendarDirections(private val context: Context) : CalendarDirections {
    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date)
        } else if (end == null) {
            return getNoEndDateText(start, false)
        }
        return getCompleteDateText(start!!, end, false)
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        val dateNightBuilder = SpannableBuilder()
        val nightCount = JodaUtils.daysBetween(start, end)

        val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
        val dateRangeText = if (forContentDescription) {
            getStartToEndDateWithDayString(context, start, end)
        } else {
            getStartDashEndDateWithDayString(context, start, end)
        }

        dateNightBuilder.append(dateRangeText)
        dateNightBuilder.append(" ")
        dateNightBuilder.append(context.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))

        if (forContentDescription) {
            return getDateAccessibilityText(context, context.getString(R.string.trip_dates_cont_desc),
                    dateNightBuilder.build().toString())
        }

        return dateNightBuilder.build().toString()
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        val selectReturnDate = Phrase.from(context, R.string.select_return_date_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToEEEMMMdBasedOnLocale(start))
                .format().toString()
        if (forContentDescription) {
            return getDateAccessibilityText(context, selectReturnDate, "")
        }
        return selectReturnDate
    }

    override fun getToolTipInstructions(end: LocalDate?): String {
        if (end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }
}