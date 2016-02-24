package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.utils.Ui

public class FlightPackageActivity : AbstractAppCompatActivity() {
    val flightsPresenter: PackageFlightPresenter by lazy {
        findViewById(R.id.package_flight_presenter) as PackageFlightPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.package_flight_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }

}