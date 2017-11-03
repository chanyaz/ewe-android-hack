package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class PackageFlightActivity : AbstractAppCompatActivity() {

    val flightsPresenter by bindView<PackageFlightPresenter>(R.id.package_flight_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Db.getPackageParams() == null) {
            setResult(Constants.PACKAGE_PARAMS_NULL_RESTORE)
            finish()
            return
        }
        setContentView(R.layout.package_flight_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }
}
