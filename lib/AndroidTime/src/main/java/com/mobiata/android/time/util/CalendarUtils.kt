package com.mobiata.android.time.util

import android.text.format.DateFormat
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import java.util.Locale

object CalendarUtils {

    fun computeVisibleDays(displayYearMonth: YearMonth, rows: Int, cols: Int): Array<Array<LocalDate?>> {
        val visibleDays = Array<Array<LocalDate?>>(rows) { arrayOfNulls(cols) }
        var firstDayOfGrid = displayYearMonth.toLocalDate(1)
        while (firstDayOfGrid.dayOfWeek != JodaUtils.getFirstDayOfWeek()) {
            firstDayOfGrid = firstDayOfGrid.minusDays(1)
        }

        for (week in 0 until rows) {
            for (dayOfWeek in 0 until cols) {
                visibleDays[week][dayOfWeek] = firstDayOfGrid.plusDays(week * cols + dayOfWeek)
            }
        }
        return visibleDays
    }

    fun formatLocalDateBasedOnLocale(date: LocalDate, pattern: String): String {
        var formattedDate: String
        try {
            formattedDate = date.toString(getLocaleBasedPattern(pattern))
        } catch (e: Exception) {
            formattedDate = date.toString(pattern)
        }
        return formattedDate
    }

    private fun getLocaleBasedPattern(pattern: String): String {
        return DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)
    }
}
