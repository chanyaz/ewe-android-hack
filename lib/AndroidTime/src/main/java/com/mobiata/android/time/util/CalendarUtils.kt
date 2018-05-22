package com.mobiata.android.time.util

import org.joda.time.LocalDate
import org.joda.time.YearMonth

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
}
