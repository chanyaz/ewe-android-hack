package com.expedia.bookings.activity

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.expedia.bookings.R
import com.expedia.bookings.viewmodel.FlightConfirmationViewModel
import javax.inject.Inject

class FlightConfirmationActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelFactory


    private val viewModel: FlightConfirmationViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FlightConfirmationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_confirmation)
    }
}
