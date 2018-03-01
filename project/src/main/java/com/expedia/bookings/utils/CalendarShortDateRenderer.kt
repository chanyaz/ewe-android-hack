package com.expedia.bookings.utils

import com.mobiata.android.time.widget.DaysOfWeekView
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarShortDateRenderer : DaysOfWeekView.DayOfWeekRenderer {

    override fun renderDayOfWeek(dayOfWeek: LocalDate.Property): String {
        val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
        return sdf.format(dayOfWeek.localDate.toDate())
    }
}
