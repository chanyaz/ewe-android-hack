package com.expedia.bookings.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock
import java.util.*

class HackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)
        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar

        val appBarLayout = findViewById(R.id.main_appbar) as AppBarLayout
        appBarLayout.addOnOffsetChangedListener({ appBarLayout: AppBarLayout, offset: Int ->
            val drawable = getDrawable(R.drawable.ic_action_bar_brand_logo)
            drawable.alpha = Math.abs(offset * 255) / appBarLayout.totalScrollRange
            myToolbar.navigationIcon = drawable
        })

        val departureClock = findViewById(R.id.departure_clock) as CustomAnalogClock
        departureClock.setTimezone(TimeZone.getTimeZone("Asia/Calcutta"))

        val arrivalClock = findViewById(R.id.arrival_clock) as CustomAnalogClock
        arrivalClock.setTimezone(TimeZone.getTimeZone("PST"))
    }

}