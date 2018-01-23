package com.expedia.bookings.utils

import android.os.Build
import com.mobiata.android.time.widget.DaysOfWeekView
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarShortDateRenderer : DaysOfWeekView.DayOfWeekRenderer {

    override fun renderDayOfWeek(dayOfWeek: LocalDate.Property): String {
        if (Build.VERSION.SDK_INT >= 18) {
            val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
            return sdf.format(dayOfWeek.localDate.toDate())
        } else if (Locale.getDefault().language == "en") {
            return dayOfWeek.asShortText.toUpperCase(Locale.getDefault()).substring(0, 1)
        }
        return DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek)
    }
}
