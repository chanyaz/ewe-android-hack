package com.expedia.bookings.utils

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Provides utility methods for formatting dates with the locale's formatting rules taken into account.
 */
object LocaleBasedDateFormatUtils {
    @JvmStatic val FLIGHT_STATS_FORMAT = "yyyy-MM-dd'T'HH:mm"

    @JvmStatic fun localDateToEEEMMMd(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "EEE, MMM d")
    }

    @JvmStatic fun localDateToMMyyyy(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "MM / yyyy")
    }

    @JvmStatic fun localDateToMMMd(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "MMM d")
    }

    @JvmStatic fun localDateToMMMMd(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "MMMM d")
    }

    @JvmStatic fun dateTimeToMMMd(date: DateTime): String {
        return localDateToMMMd(date.toLocalDate())
    }

    @JvmStatic fun dateTimeToMMMdhmma(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "MMM d, h:mm a")
    }

    @JvmStatic fun dateTimeToEEEMMMdhmma(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEE, MMM d - h:mm a")
    }

    @JvmStatic fun dateTimeTohmma(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "h:mma")
    }

    @JvmStatic fun dateTimeToEEEMMMd(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEE, MMM d")
    }

    @JvmStatic fun dateTimeToEEEEMMMd(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEEE, MMM d")
    }

    @JvmStatic fun dateTimeToEEEMMMddyyyy(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "EEE MMM dd, yyyy")
    }

    @JvmStatic fun dateTimeToEEEMMMddyyyy(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEE MMM dd, yyyy")
    }

    @JvmStatic fun dateTimeToEEEMMMdd(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEE MMM dd")
    }

    /**
     * Convert a date string formatted as "yyyy-MM-dd" into a string with format "EEE MMM dd, yyyy"
     * @param date date string
     * @return reformatted date string
     */
    @JvmStatic fun stringToEEEMMMddyyyy(date: String?): String {
        if (date == null) {
            return ""
        }
        val parser = DateTimeFormat.forPattern("yyyy-MM-dd")
        return dateTimeToEEEMMMddyyyy(parser.parseDateTime(date))
    }

    /**
     * Convert the given date ints to a formatted date with year and shortened month. The date follows the locale's date
     * formatting rules.
     * @param year year as int
     * @param month month in year as int
     * @param day day in month as int
     * @return formatted date string
     */
    @JvmStatic fun formatBirthDate(year: Int, month: Int, day: Int): String {
        return formatLocalDateBasedOnLocale(LocalDate(year, month, day), "MMM d, yyyy")
    }

    @JvmStatic fun getDeviceTimeFormat(context: Context): String {
        return (android.text.format.DateFormat.getTimeFormat(context) as SimpleDateFormat).toPattern()
    }

    @JvmStatic fun parseFlightStatsDateTime(dateTime: String): LocalDateTime {
        // It turns out it's faster just to parse this by hand
        val year = Integer.parseInt(dateTime.substring(0, 4))
        val month = Integer.parseInt(dateTime.substring(5, 7))
        val day = Integer.parseInt(dateTime.substring(8, 10))
        val hour = Integer.parseInt(dateTime.substring(11, 13))
        val minute = Integer.parseInt(dateTime.substring(14, 16))

        return LocalDateTime(year, month, day, hour, minute)
    }

    private fun formatLocalDateBasedOnLocale(date: LocalDate, pattern: String): String {
        var formattedDate: String
        try {
            formattedDate = date.toString(getLocaleBasedPattern(pattern))
        } catch (e: Exception) {
            formattedDate = date.toString(pattern)
        }
        return formattedDate
    }

    private fun formatDateTimeBasedOnLocale(dateTime: DateTime, pattern: String): String {
        var formattedDateTime: String
        try {
            formattedDateTime = dateTime.toString(getLocaleBasedPattern(pattern))
        } catch (e: Exception) {
            formattedDateTime = dateTime.toString(pattern)
        }
        return formattedDateTime
    }

    private fun getLocaleBasedPattern(pattern: String): String {
        return DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)
    }
}
