package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.utils.AlertDialogUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DeeplinkSharedPrefParserUtils
import com.expedia.bookings.utils.Ui

class PackageFlightActivity : AbstractAppCompatActivity() {

    val flightsPresenter: PackageFlightPresenter by lazy {
        val presenter = findViewById(R.id.package_flight_presenter) as PackageFlightPresenter
        presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Db.getPackageParams() == null) {
            setResult(Constants.PACKAGE_PARAMS_NULL_RESTORE)
            finish()
            return
        }
        setContentView(R.layout.package_flight_activity)
        Ui.showTransparentStatusBar(this)
        if (DeeplinkSharedPrefParserUtils.isDeeplink) {
            AlertDialogUtils.showBookmarkDialog(this)
        }
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }
}
