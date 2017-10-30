package com.expedia.bookings.shared.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate

class CalendarDateFormatter {
    companion object {
        fun formatStartToEnd(context: Context, start: LocalDate, end: LocalDate): String {
            // need to explicitly use "to" for screen readers
            return Phrase.from(context, R.string.start_to_end_date_range_cont_desc_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(start))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(end))
                    .format().toString()
        }

        fun formatStartDashEnd(context: Context, start: LocalDate, end: LocalDate): String {
            return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(start))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(end))
                    .format().toString()
        }

        fun getDateAccessibilityText(context: Context, datesLabel: String, durationDescription: String): String {
            return Phrase.from(context, R.string.search_dates_cont_desc_TEMPLATE)
                    .put("dates_label", datesLabel)
                    .put("duration_description", durationDescription).format().toString()
        }
    }
}