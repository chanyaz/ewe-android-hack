package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui

public class PackageActivity : AppCompatActivity() {
    val packagePresenter: PackagePresenter by lazy {
        findViewById(R.id.hotel_presenter) as PackagePresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultPackageComponents()
        setContentView(R.layout.package_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.HOTEL_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    packagePresenter.bundlePresenter.viewModel.flightParamsObservable.onNext(Db.getPackageParams())
                    packagePresenter.bundlePresenter.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
                    return
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!packagePresenter.back()) {
            super.onBackPressed()
        }
    }
}