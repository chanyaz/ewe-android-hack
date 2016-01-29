package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.vm.PackageSearchType

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
                }
            }
            Constants.PACKAGE_FLIGHT_DEPARTURE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    packagePresenter.bundlePresenter.viewModel.flightParamsObservable.onNext(Db.getPackageParams())
                    packagePresenter.bundlePresenter.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                }
            }

            Constants.PACKAGE_FLIGHT_ARRIVAL_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    packagePresenter.bundlePresenter.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)

                    val params = PackageCreateTripParams.fromPackageSearchParams(Db.getPackageParams())
                    if (params.isValid)
                        packagePresenter.bundlePresenter.checkoutPresenter.createTripViewModel.tripParams.onNext(params)
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