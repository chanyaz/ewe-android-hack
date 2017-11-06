package com.expedia.bookings.utils

import android.text.format.DateFormat
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.Locale

object LocaleBasedDateFormatUtils {

    private val isUserBucketed: Boolean
        get() = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLocaleBasedDateFormatting)

    @JvmStatic fun localDateToEEEMMMd(date: LocalDate): String {
        if (isUserBucketed) {
            return formatLocalDateBasedOnLocale(date, "EEE, MMM d")
        } else {
            return date.toString("EEE, MMM d")
        }
    }

    @JvmStatic fun localDateToMMyyyy(date: LocalDate): String {
        if (isUserBucketed) {
            return formatLocalDateBasedOnLocale(date, "MM / yyyy")
        } else {
            return date.toString("MM / yyyy")
        }
    }

    @JvmStatic fun localDateToMMMd(date: LocalDate): String {
        if (isUserBucketed) {
            return formatLocalDateBasedOnLocale(date, "MMM d")
        } else {
            return date.toString("MMM d")
        }
    }

    @JvmStatic fun localDateToMMMMd(date: LocalDate): String {
        if (isUserBucketed) {
            return formatLocalDateBasedOnLocale(date, "MMMM d")
        } else {
            return date.toString("MMMM d")
        }
    }

    @JvmStatic fun dateTimeToMMMd(date: DateTime): String {
        if (isUserBucketed) {
            return localDateToMMMd(date.toLocalDate())
        } else {
            return date.toString("MMM d")
        }
    }

    @JvmStatic fun dateTimeToMMMdhmma(date: DateTime): String {
        if (isUserBucketed) {
            return formatDateTimeBasedOnLocale(date, "MMM d, h:mm a")
        } else {
            return date.toString("MMM d, h:mm a")
        }
    }

    @JvmStatic fun dateTimeToEEEMMMdhmma(date: DateTime): String {
        if (isUserBucketed) {
            return formatDateTimeBasedOnLocale(date, "EEE, MMM d - h:mm a")
        } else {
            return date.toString("EEE, MMM d - h:mm a")
        }
    }

    @JvmStatic fun dateTimeTohmma(date: DateTime): String {
        if (isUserBucketed) {
            return formatDateTimeBasedOnLocale(date, "h:mma")
        } else {
            return date.toString("h:mma")
        }
    }

    @JvmStatic fun dateTimeToEEEMMMd(date: DateTime): String {
        if (isUserBucketed) {
            return formatDateTimeBasedOnLocale(date, "EEE, MMM d")
        } else {
            return date.toString("EEE, MMM d")
        }
    }

    @JvmStatic fun dateTimeToyyyyMMMd(date: DateTime): String {
        if (isUserBucketed) {
            return formatDateTimeBasedOnLocale(date, "yyyy-MM-d")
        } else {
            return date.toString("yyyy-MM-d")
        }
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