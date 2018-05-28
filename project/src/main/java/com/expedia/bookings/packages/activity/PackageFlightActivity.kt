package com.expedia.bookings.packages.activity

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.packages.presenter.PackageFlightPresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.AbstractAppCompatActivity

class PackageFlightActivity : AbstractAppCompatActivity() {

    val flightsPresenter by bindView<PackageFlightPresenter>(R.id.package_flight_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Db.sharedInstance.packageParams == null) {
            setResult(Constants.PACKAGE_PARAMS_NULL_RESTORE)
            finish()
            return
        }
        setContentView(R.layout.package_flight_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            Db.getCachedPackageResponse()?.let {
                Db.setPackageResponse(it)
                Db.setCachedPackageResponse(null)
            }
            super.onBackPressed()
        }
    }
}
