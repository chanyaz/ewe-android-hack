package com.expedia.bookings.hotel.util

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarDirections
import com.expedia.bookings.shared.util.CalendarDateFormatter
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

open class HotelCalendarDirections(private val context: Context) : CalendarDirections {
    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getString(R.string.select_checkin_date)
        } else if (end == null) {
            return getNoEndDateText(start, false)
        }
        return getCompleteDateText(start!!, end, false)
    }

    override fun getToolTipInstructions(end: LocalDate?) : String {
        if (end == null) {
            return context.getString(R.string.hotel_calendar_tooltip_bottom)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        val selectCheckoutText = context.getString(R.string.select_checkout_date_TEMPLATE,
                LocaleBasedDateFormatUtils.localDateToMMMd(start!!))
        if (forContentDescription) {
            return CalendarDateFormatter.getDateAccessibilityText(context, selectCheckoutText, "")
        }
        return selectCheckoutText
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        val dateNightText = getDateNightText(start, end, forContentDescription)
        if (forContentDescription) {
            return CalendarDateFormatter.getDateAccessibilityText(context, context.getString(R.string.select_dates),
                    dateNightText.toString())
        }
        return dateNightText.toString()
    }

    open fun getToolTipTitle(start: LocalDate?, end: LocalDate?) : String {
        if (start == null && end == null) {
            return context.getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return LocaleBasedDateFormatUtils.localDateToMMMd(start!!)
        }
        return CalendarDateFormatter.formatStartDashEnd(context, start!!, end)
    }

    open fun getToolTipContDesc(startDate: LocalDate?, endDate: LocalDate?) : String {
        if (startDate == null && endDate == null) {
            return context.getString(R.string.select_dates_proper_case)
        } else if (endDate == null) {
            return Phrase.from(context, R.string.calendar_start_date_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate!!))
                    .put("instructiontext", getToolTipInstructions(endDate))
                    .format().toString()
        }
        return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                .put("selecteddate", CalendarDateFormatter.formatStartToEnd(context, startDate!!, endDate))
                .format().toString()
    }

    private fun getDateNightText(start: LocalDate, end: LocalDate, isContentDescription: Boolean): CharSequence {
        val dateNightBuilder = SpannableBuilder()
        val nightCount = JodaUtils.daysBetween(start, end)

        val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)

        val dateRangeText = if (isContentDescription) {
            CalendarDateFormatter.formatStartToEnd(context, start, end)
        } else {
            CalendarDateFormatter.formatStartDashEnd(context, start, end)
        }

        dateNightBuilder.append(dateRangeText)
        dateNightBuilder.append(" ")
        dateNightBuilder.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))

        return dateNightBuilder.build()
    }
}