package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.presenter.flight.FlightPresenter
import com.expedia.bookings.utils.Ui

class FlightActivity : AbstractAppCompatActivity() {
    val flightsPresenter: FlightPresenter by lazy {
        findViewById(R.id.flight_presenter) as FlightPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultFlightComponents()
        setContentView(R.layout.flight_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }

}