package com.expedia.bookings.utils
import android.content.Context
import android.content.res.Resources
import org.joda.time.LocalDate
import org.joda.time.Minutes
import org.joda.time.ReadableInstant
import java.util.concurrent.TimeUnit
import android.text.format.DateUtils
import com.expedia.bookings.R
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils.getDeviceTimeFormat
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Provides methods for determining the range between two date types. The expected output of each of these methods is a
 * formatted string or a numerical value representing the range.
 */
object DateRangeUtils {
    /**
     * This is the equivalent of android.text.format.getDateFormat(), when used in ApiDateUtils
     */
    @JvmStatic val FLAGS_DATE_NUMERIC = DateUtils.FORMAT_NUMERIC_DATE

    /**
     * This is the equivalent of android.text.format.getLongDateFormat(), when used in ApiDateUtils
     */
    @JvmStatic val FLAGS_LONG_DATE_FORMAT = (DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
            or DateUtils.FORMAT_SHOW_YEAR)

    /**
     * Formatted Date example: Apr 12
     */
    private val FLAGS_DATE_ABBREV_MONTH = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH

    // #9770: Add an hour of buffer so that the date range is always > the number of days
    private val DATE_RANGE_BUFFER = TimeUnit.HOURS.toMillis(1)

    /**
     * This is the equivalent of android.text.format.getTimeFormat(), when used in ApiDateUtils
     */
    @JvmStatic val FLAGS_TIME_FORMAT = DateUtils.FORMAT_SHOW_TIME

    @JvmStatic val FLAGS_MEDIUM_DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or
            DateUtils.FORMAT_SHOW_YEAR

    /**
     * Format the start and end dates as a range with " - " between them. The month is abbreviated to 3 letters. Dates
     * are simplified if they are within the same month, year, etc. For example: Feb 19 – 20, 2018" or
     * "Feb 19 – Mar 19, 2018".
     * @param context for the string formatting
     * @param startDate start date in range
     * @param endDate end date in range
     * @return date range as formatted string
     */
    @JvmStatic
    fun formatDateRangeHotelConfirmation(context: Context, startDate: LocalDate, endDate: LocalDate): String {
        val start = startDate.toDateTimeAtStartOfDay().millis
        val end = endDate.toDateTimeAtStartOfDay().millis + DATE_RANGE_BUFFER
        return DateUtils.formatDateRange(context, start, end, FLAGS_DATE_ABBREV_MONTH)
    }

    /**
     * Converts two LocalDates to a String with the dates as a date/time range. Dates are in the format "MMM d, hh:mm a".
     * An example is "Apr 12, 4:00 PM - Apr 13, 8:00 AM". If round trip is false, the formatted start date is just shown.
     * @param context for string formatting
     * @param startDate start date in range
     * @param startMillis start millis in range
     * @param endDate end date in range
     * @param endMillis end millis in range
     * @param isRoundTrip true if roundtrip and both dates should be displayed
     * @return date range as formatted string
     */
    @JvmStatic
    fun formatRailDateTimeRange(
        context: Context,
        startDate: LocalDate?,
        startMillis: Int,
        endDate: LocalDate?,
        endMillis: Int,
        isRoundTrip: Boolean?
    ): String {
        val startDateTime = ApiDateUtils
                .localDateAndMillisToDateTime(startDate, startMillis)
        if (isRoundTrip == true) {
            val endDateTime = ApiDateUtils
                    .localDateAndMillisToDateTime(endDate, endMillis)
            return formatStartEndDateTimeRange(context, startDateTime, endDateTime)
        } else {
            return LocaleBasedDateFormatUtils.dateTimeToMMMd(startDateTime) + ", " + DateUtils
                    .formatDateTime(context, startDateTime.millis, FLAGS_TIME_FORMAT)
        }
    }

    /**
     * Converts two LocalDates to a formatted string with the dates separated by " - ". The dates are in format "MMM d".
     * If the end date is null, just with formatted start date is returned.
     * If the start date is null, an empty string is returned.
     * @param context for string formatting
     * @param startDate start date in range
     * @param endDate end date in range
     * @return date range as formatted string
     */
    @JvmStatic
    fun formatRailDateRange(context: Context, startDate: LocalDate?, endDate: LocalDate?): String {
        return when {
            startDate == null -> ""
            endDate == null -> Phrase.from(context, R.string.calendar_instructions_date_rail_one_way_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate)).format().toString()
            else -> Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(endDate)).format().toString()
        }
    }

    /**
     * Convenience method for formatting date range in hotelsv2 from 2015-10-1 to Oct 01, 2015
     */
    @JvmStatic
    fun formatHotelsV2DateRange(context: Context, checkinDate: String, checkoutDate: String): String {
        val parser = DateTimeFormat.forPattern("yyyy-MM-dd")
        val checkinDateTime = parser.parseDateTime(checkinDate)
        val checkoutDateTime = parser.parseDateTime(checkoutDate)

        val formatter = DateTimeFormat.forPattern("MMM dd, yyyy")
        return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                .put("startdate", formatter.print(checkinDateTime)).put("enddate", formatter.print(checkoutDateTime))
                .format().toString()
    }

    /**
     * Formats a date range with " to " between the dates. The dates are in the format "EEE MMM dd, yyyy".
     * @param context for string formatting
     * @param checkinDate start date in range formatted as "yyyy-MM-dd"
     * @param checkoutDate end date in range formatted as "yyyy-MM-dd"
     * @return formatted date string
     */
    @JvmStatic
    fun formatPackageDateRangeContDesc(context: Context, checkinDate: String, checkoutDate: String): String {
        return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate,
                R.string.start_to_end_date_range_cont_desc_TEMPLATE)
    }

    /**
     * Formats a date range with " - " between the dates. The dates are formatted as "EEE MMM dd, yyyy".
     * @param context for string formatting
     * @param checkinDate start date in range formatted as "yyyy-MM-dd"
     * @param checkoutDate end date in range formatted as "yyyy-MM-dd"
     * @return formatted date string
     */
    @JvmStatic
    fun formatPackageDateRange(context: Context, checkinDate: String, checkoutDate: String?): String {
        if (checkoutDate == null) {
            return ""
        }
        return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate,
                R.string.start_dash_end_date_range_TEMPLATE)
    }

    /**
     * Formats two times with " - " between them. The time format is "hh:mm:ss" (based on locale). Dates are not shown,
     * so even it's expected that start and end are on the same day.
     */
    @JvmStatic
    fun formatTimeInterval(context: Context, start: DateTime, end: DateTime): CharSequence {
        val dateFormat = getDeviceTimeFormat(context)
        val formattedStart = JodaUtils.format(start, dateFormat)
        val formattedEnd = JodaUtils.format(end, dateFormat)
        return Phrase.from(context, R.string.date_time_range_TEMPLATE)
                .put("from_date_time", formattedStart)
                .put("to_date_time", formattedEnd)
                .format().toString()
    }

    /**
     * Formats the passed duration (given in number of minutes) as a readable String.
     *
     * @param r
     * @param durationMins number of minutes
     * @return the duration, or the empty string if duration <= 0
     */
    @JvmStatic fun formatDuration(r: Resources, durationMins: Int): String {
        val minutes = Math.abs(durationMins % 60)
        val hours = Math.abs(durationMins / 60)
        if (hours > 0 && minutes > 0) {
            return r.getString(R.string.hours_minutes_template, hours, minutes)
        } else if (hours > 0) {
            return r.getString(R.string.hours_template, hours)
        } else if (minutes >= 0) {
            return r.getString(R.string.minutes_template, minutes)
        } else {
            return ""
        }
    }

    @JvmStatic fun formatDurationDaysHoursMinutes(context: Context, durationMins: Int): String {
        if (durationMins < 0) {
            return ""
        }
        val minutes = Math.abs(durationMins % 60)
        val hours = Math.abs(durationMins / 60 % 24)
        val days = Math.abs(durationMins / 24 / 60)
        if (days > 0 && hours > 0 && minutes > 0) {
            return Phrase.from(context, R.string.flight_duration_days_hours_minutes_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0 && hours > 0) {
            return Phrase.from(context, R.string.flight_duration_days_hours_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours).format().toString()
        } else if (days > 0 && minutes > 0) {
            return Phrase.from(context, R.string.flight_duration_days_minutes_TEMPLATE)
                    .put("days", days)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0) {
            return Phrase.from(context, R.string.flight_duration_days_TEMPLATE)
                    .put("days", days).format().toString()
        } else if (hours > 0 && minutes > 0) {
            return Phrase.from(context, R.string.flight_duration_hours_minutes_TEMPLATE)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (hours > 0) {
            return Phrase.from(context, R.string.flight_duration_hours_TEMPLATE)
                    .put("hours", hours).format().toString()
        } else if (minutes > 0) {
            return Phrase.from(context, R.string.flight_duration_minutes_TEMPLATE)
                    .put("minutes", minutes).format().toString()
        } else {
            return ""
        }
    }

    @JvmStatic fun getDurationContDescDaysHoursMins(context: Context, durationMinutes: Int): String? {
        if (durationMinutes <= 0) {
            return null
        }
        val minutes = Math.abs(durationMinutes % 60)
        val hours = Math.abs(durationMinutes / 60 % 24)
        val days = Math.abs(durationMinutes / 24 / 60)
        var contDesc = ""
        if (days > 0 && hours > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_hours_minutes_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0 && hours > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_hours_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours).format().toString()
        } else if (days > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_minutes_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_cont_desc_TEMPLATE)
                    .put("days", days).format().toString()
        } else if (hours > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_hours_minutes_cont_desc_TEMPLATE)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (hours > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_hours_cont_desc_TEMPLATE)
                    .put("hours", hours).format().toString()
        } else if (minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_minutes_cont_desc_TEMPLATE)
                    .put("minutes", minutes).format().toString()
        }
        return contDesc
    }

    /**
     * Returns the difference between the two times in minutes
     *
     * @param cal1 the first date/time
     * @param cal2 the second date/time
     * @return the difference between them in minutes
     */
    @JvmStatic
    fun getMinutesBetween(cal1: ReadableInstant, cal2: ReadableInstant): Int {
        return Minutes.minutesBetween(cal1, cal2).minutes
    }

    /**
     * Converts two DateTimes to a String with the dates as a date/time range. Dates are in the format "MMM d, hh:mm a".
     * An example is "Apr 12, 4:00 PM - Apr 13, 8:00 AM". If the end date is null, the start date is show with the text
     * "- Select return date" appended.
     * @param context context to build string from
     * @param startDateTime start date in range
     * @param endDateTime end date in range
     * @return date range as formatted string
     */
    private fun formatStartEndDateTimeRange(context: Context, startDateTime: DateTime, endDateTime: DateTime?): String {
        val formattedStartDateTime = LocaleBasedDateFormatUtils.dateTimeToMMMd(startDateTime) + ", " + DateUtils
                .formatDateTime(context, startDateTime.millis, FLAGS_TIME_FORMAT)
        if (endDateTime != null) {
            val formattedEndDateTime = LocaleBasedDateFormatUtils.dateTimeToMMMd(endDateTime) + ", " + DateUtils
                    .formatDateTime(context, endDateTime.millis, FLAGS_TIME_FORMAT)
            return Phrase.from(context, R.string.date_time_range_TEMPLATE)
                    .put("from_date_time", formattedStartDateTime)
                    .put("to_date_time", formattedEndDateTime)
                    .format().toString()
        }

        return Phrase.from(context.resources, R.string.select_return_date_TEMPLATE)
                .put("startdate", formattedStartDateTime)
                .format().toString()
    }

    /**
     * Formats a date range with the given template. The dates are formatted as "EEE MMM dd, yyyy".
     * @param context for string formatting
     * @param checkinDate start date in range formatted as "yyyy-MM-dd"
     * @param checkoutDate end date in range formatted as "yyyy-MM-dd"
     * @param stringResID string template which uses two dates strings as parameters
     * @return formatted date string
     */
    private fun formatPackageDateRangeTemplate(
        context: Context,
        checkinDate: String,
        checkoutDate: String,
        stringResID: Int
    ): String {
        val parser = DateTimeFormat.forPattern("yyyy-MM-dd")
        val checkinDateTime = dateTimeToEEEMMMddyyyy(parser.parseDateTime(checkinDate))
        val checkoutDateTime = dateTimeToEEEMMMddyyyy(parser.parseDateTime(checkoutDate))

        return Phrase.from(context, stringResID)
                .put("startdate", checkinDateTime).put("enddate", checkoutDateTime).format().toString()
    }
}
