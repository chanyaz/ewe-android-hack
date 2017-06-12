package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui

class FlightItinDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.activity_itin_card_details)
    }
}
