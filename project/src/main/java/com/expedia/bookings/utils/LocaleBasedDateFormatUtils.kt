package com.expedia.bookings.utils

import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.Locale

object LocaleBasedDateFormatUtils {

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

    @JvmStatic fun dateTimeToyyyyMMMd(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "yyyy-MM-d")
    }

    @JvmStatic fun dateTimeToEEEMMMddyyyy(date: LocalDate): String {
        return formatLocalDateBasedOnLocale(date, "EEE MMM dd, yyyy")
    }

    @JvmStatic fun dateTimeToEEEMMMddyyyy(date: DateTime): String {
        return formatDateTimeBasedOnLocale(date, "EEE MMM dd, yyyy")
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
