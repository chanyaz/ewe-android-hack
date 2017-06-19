package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.hackathon1.FlightTripWidget
import com.expedia.bookings.utils.bindView

class HackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)
    }

}