package com.expedia.bookings.presenter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView

open class BaseSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)

    fun styleCalendar() {
        BaseSearchPresenter.styleCalendar(context, calendar, monthView, dayOfWeek)
    }

    companion object {
        @JvmStatic fun styleCalendar(context: Context, calendar: CalendarPicker, monthView: MonthView, dayOfWeek: DaysOfWeekView) {
            monthView.setTextEqualDatesColor(Color.WHITE)
            monthView.setMaxTextSize(context.resources.getDimension(R.dimen.car_calendar_month_view_max_text_size))
            dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

            calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
            dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
            monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
            monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))
        }
    }
}

