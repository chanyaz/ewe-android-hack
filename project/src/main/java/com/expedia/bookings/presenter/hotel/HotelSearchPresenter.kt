package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.presenter.Presenter
import com.mobiata.android.time.widget.CalendarPicker
import org.joda.time.LocalDate
import com.expedia.bookings.R
import android.widget.ToggleButton

import com.expedia.bookings.widget.TravelerPicker

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CalendarPicker.DateSelectionChangedListener, TravelerPicker.TravelersUpdatedListener {

    var selectDate: ToggleButton? = null
    var selectTraveler: ToggleButton? = null
    var calendar: CalendarPicker? = null
    var traveler: TravelerPicker? = null

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        calendar = findViewById(R.id.calendar) as CalendarPicker
        traveler = findViewById(R.id.traveler_view) as TravelerPicker
        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        selectDate = findViewById(R.id.select_date) as ToggleButton
        selectTraveler: ToggleButton = findViewById(R.id.select_traveler) as ToggleButton

        toolbar.inflateMenu(R.menu.cars_search_menu)
        traveler?.setTravelerUpdatedListener(this)
        calendar?.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar?.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar?.setDateChangedListener(this)
        selectDate?.setOnClickListener {
            calendar?.setVisibility(View.VISIBLE)
            traveler?.setVisibility(View.GONE)
        }

        selectTraveler?.setOnClickListener {
            calendar?.setVisibility(View.GONE)
            traveler?.setVisibility(View.VISIBLE)
        }
    }

    override fun onDateSelectionChanged(start: LocalDate?, end: LocalDate?) {
        var displayText: String = if (start == null && end == null) "Select Dates"
        else if (end == null) start.toString()
        else start.toString() + " to " + end.toString()

        selectDate?.setText(displayText)
        selectDate?.setTextOff(displayText)
        selectDate?.setTextOn(displayText)
    }

    override fun onTravelerUpdate(text: String) {
        selectTraveler?.setTextOn(text)
        selectTraveler?.setTextOff(text)
        selectTraveler?.setText(text)
    }
}

