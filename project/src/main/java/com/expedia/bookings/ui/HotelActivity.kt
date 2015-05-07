package com.expedia.bookings.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.mobiata.android.time.widget.CalendarPicker
import org.joda.time.LocalDate

public class HotelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel)
    }
}

