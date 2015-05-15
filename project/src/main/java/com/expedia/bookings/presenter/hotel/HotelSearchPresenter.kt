package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.TravelerPicker
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import kotlin.properties.Delegates

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CalendarPicker.DateSelectionChangedListener, TravelerPicker.TravelersUpdatedListener,CalendarPicker.YearMonthDisplayedChangedListener {

    val selectDate: ToggleButton by Delegates.lazy {
        findViewById(R.id.select_date) as ToggleButton
    }

    val selectTraveler: ToggleButton by Delegates.lazy {
        findViewById(R.id.select_traveler) as ToggleButton
    }

    val calendar: CalendarPicker by Delegates.lazy {
        findViewById(R.id.calendar) as CalendarPicker
    }

    val monthView: MonthView by Delegates.lazy {
        findViewById(R.id.month) as MonthView
    }

    val traveler: TravelerPicker by Delegates.lazy {
        findViewById(R.id.traveler_view) as TravelerPicker
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar

        toolbar.inflateMenu(R.menu.cars_search_menu)
        traveler.setTravelerUpdatedListener(this)
        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener(this)
        monthView.setTextEqualDatesColor(Color.WHITE)
        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            hideToolTip()
            traveler.setVisibility(View.VISIBLE)
        }
    }

    override fun onDateSelectionChanged(start: LocalDate?, end: LocalDate?) {
        var displayText: String = if (start == null && end == null) getResources().getString(R.string.select_dates_proper_case)
        else if (end == null) DateUtils.localDateToMMMd(start)
        else getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))

        selectDate.setText(displayText)
        selectDate.setTextOff(displayText)
        selectDate.setTextOn(displayText)

        calendar.setToolTipText(displayText, if (end == null) getResources().getString(R.string.calendar_tooltip_bottom_select_return_date)
                                              else getResources().getString(R.string.calendar_tooltip_bottom_drag_to_modify))

    }

    override fun onTravelerUpdate(text: String) {
        selectTraveler.setTextOn(text)
        selectTraveler.setTextOff(text)
        selectTraveler.setText(text)
    }

    override fun onYearMonthDisplayed(yearMonth: YearMonth?) {
        hideToolTip()
    }

    fun hideToolTip() {
        calendar.hideToolTip()
    }
}

