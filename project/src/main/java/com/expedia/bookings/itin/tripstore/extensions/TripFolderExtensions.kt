package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.DateTime

fun TripFolder.startDate(): DateTime {
    val epochSeconds = startTime.epochSeconds
    val timezoneOffset = startTime.timeZoneOffsetSeconds
    return JodaUtils.fromMillisAndOffset(epochSeconds * 1000, timezoneOffset * 1000)
}

fun TripFolder.endDate(): DateTime {
    val epochSeconds = endTime.epochSeconds
    val timezoneOffset = endTime.timeZoneOffsetSeconds
    return JodaUtils.fromMillisAndOffset(epochSeconds * 1000, timezoneOffset * 1000)
}

fun abbreviatedDateRange(startDate: DateTime, endDate: DateTime, stringSource: StringSource): String {
    val tripCrossesYear = (startDate.year != endDate.year)
    val tripCrossesMonth = (startDate.monthOfYear != endDate.monthOfYear)
    val startText: String
    val endText: String

    if (tripCrossesYear || endDate < DateTime.now()) {
        startText = LocaleBasedDateFormatUtils.dateTimeToMMMddyyyy(startDate)
        endText = LocaleBasedDateFormatUtils.dateTimeToMMMddyyyy(endDate)
    } else {
        startText = LocaleBasedDateFormatUtils.dateTimeToMMMd(startDate)
        endText = if (tripCrossesMonth) {
            LocaleBasedDateFormatUtils.dateTimeToMMMd(endDate)
        } else {
            endDate.dayOfMonth.toString()
        }
    }
    return stringSource.fetchWithPhrase(
            R.string.trip_folder_abbreviated_date_range_TEMPLATE,
            mapOf("startdate" to startText, "enddate" to endText)
    )
}
