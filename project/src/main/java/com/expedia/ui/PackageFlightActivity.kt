package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.PackageFlightContainerPresenter
import com.expedia.bookings.utils.Ui

class PackageFlightActivity : AbstractAppCompatActivity() {

    val flightContainerPresenter: PackageFlightContainerPresenter by lazy {
        findViewById(R.id.package_flight_container) as PackageFlightContainerPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.package_flight_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!flightContainerPresenter.back()) {
            super.onBackPressed()
        }
    }
}
