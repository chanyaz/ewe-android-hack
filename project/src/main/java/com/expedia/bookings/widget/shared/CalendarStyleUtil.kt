package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Color
import com.expedia.bookings.R
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.Font
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView

class CalendarStyleUtil {
    //todo this should probably live in CalendarPicker itself?
    companion object {
        @JvmStatic fun style(context: Context, calendar: CalendarPicker, monthView: MonthView, dayOfWeek: DaysOfWeekView) {
            monthView.setTextEqualDatesColor(Color.WHITE)
            monthView.setMaxTextSize(context.resources.getDimension(R.dimen.calendar_month_view_max_text_size))
            dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

            calendar.setMonthHeaderTypeface(Font.ROBOTO_REGULAR.typeface)
            dayOfWeek.setTypeface(Font.ROBOTO_REGULAR.typeface)
            monthView.setDaysTypeface(Font.ROBOTO_LIGHT.typeface)
            monthView.setTodayTypeface(Font.ROBOTO_MEDIUM.typeface)
        }
    }
}
