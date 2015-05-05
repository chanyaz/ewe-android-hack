package com.expedia.bookings.ui

import com.expedia.bookings.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.support.v7.widget.Toolbar
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.ToggleButton
import org.joda.time.LocalDate
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.mobiata.android.time.widget.CalendarPicker

public class HotelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_search)
        var calendar: CalendarPicker = findViewById(R.id.calendar) as CalendarPicker
        var traveler: LinearLayout = findViewById(R.id.traveler_view) as LinearLayout
        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        var selectDate: ToggleButton = findViewById(R.id.select_date) as ToggleButton
        var selectTraveler: ToggleButton = findViewById(R.id.select_traveler) as ToggleButton

        toolbar.inflateMenu(R.menu.cars_search_menu)
        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))

        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            traveler.setVisibility(View.VISIBLE)
        }

    }
}

