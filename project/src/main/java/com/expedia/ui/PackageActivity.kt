package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.PackageSearchType

class PackageActivity : AbstractAppCompatActivity() {

    var changedOutboundFlight = false;

    val packagePresenter: PackagePresenter by lazy {
        findViewById(R.id.package_presenter) as PackagePresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultPackageComponents()
        setContentView(R.layout.package_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            val obj = packagePresenter.backStack.peek()
            packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.collapseSelectedHotel()
            packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.collapseFlightDetails()
            packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.collapseFlightDetails()

            if (Db.getPackageParams().isChangePackageSearch() && obj !is Intent) {
                onBackPressed()
            } else {
                PackagesTracking().trackViewBundlePageLoad()
                if (obj is Intent && obj.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
                    packagePresenter.bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                    packagePresenter.bundlePresenter.getCheckoutPresenter().toggleCheckoutButton(false)
                    packagePresenter.bundlePresenter.bundleWidget.toggleMenuObservable.onNext(false)

                    //revert bundle view to be the state loaded inbound flights
                    packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectInbound()
                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                } else if (obj is Intent && obj.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
                    //revert bundle view to be the state loaded inbound flights
                    packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectOutbound()
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                } else if (packagePresenter.backStack.size == 2) {
                    //revert bundle view to be the state loaded hotels
                    packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectHotel()
                    packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
                }
            }
            return
        }

        when (requestCode) {
            Constants.HOTEL_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data?.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)?.equals(ApiError.Code.PACKAGE_SEARCH_ERROR.name) ?: false) {
                        packagePresenter.hotelOffersErrorObservable.onNext(ApiError.Code.PACKAGE_SEARCH_ERROR)
                    } else {
                        //is is change hotel search, call createTrip, otherwise start outbound flight search
                        if (!Db.getPackageParams().isChangePackageSearch()) {
                            packageFlightSearch()
                            val intent = Intent(this, PackageHotelActivity::class.java)
                            intent.putExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM, true)
                            intent.putExtra(Constants.REQUEST, Constants.HOTEL_REQUEST_CODE)
                            packagePresenter.backStack.push(intent)
                        } else {
                            packageCreateTrip()
                        }
                        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
                    }
                }
            }
            Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    packageFlightSearch()
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedOutboundFlight())

                    val intent = Intent(this, PackageFlightActivity::class.java)
                    intent.putExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT, true)
                    intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE)
                    packagePresenter.backStack.push(intent)

                    if (Db.getPackageParams().isChangePackageSearch()) {
                        changedOutboundFlight = true
                    }
                }
            }

            Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (!Db.getPackageParams().isChangePackageSearch()) {
                        val intent = Intent(this, PackageFlightActivity::class.java)
                        intent.putExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT, true)
                        intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE)
                        packagePresenter.backStack.push(intent)
                        packagePresenter.backStack.push(packagePresenter.bundlePresenter)
                        packagePresenter.bundlePresenter.show(BaseOverviewPresenter.BundleDefault())
                    } else {
                        //If change flight, remove changed outbound flight intent
                        packagePresenter.backStack.pop()
                    }

                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedInboundFlight())
                    packageCreateTrip()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (packagePresenter.backStack.peek() is PackageOverviewPresenter && Db.getPackageParams()?.isChangePackageSearch() ?: false) {
            Db.getPackageParams().pageType = null
            if (changedOutboundFlight) {
                //when cancel changed outbound flight, packagePresenter's backStack is:
                //Search, Overview, Hotel Intent, Outbound Flight Intent, InboundFlightIntent, Overview
                //Remove last three objects in the backStack, so that it backs to hotel InfoSite.
                changedOutboundFlight = false
                packagePresenter.show(Intent(this, PackageHotelActivity::class.java), Presenter.FLAG_CLEAR_TOP)
                packagePresenter.bundlePresenter.bundleWidget.revertBundleViewAfterChangedOutbound()
            } else {
                packageCreateTrip()
                packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
                packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
                return
            }
        }
        if (!packagePresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()

        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        }
    }

    private fun packageFlightSearch() {
        PackagesTracking().trackViewBundlePageLoad()
        packagePresenter.bundlePresenter.bundleWidget.viewModel.flightParamsObservable.onNext(Db.getPackageParams())
    }

    private fun packageCreateTrip() {
        Db.getPackageParams().pageType = null
        val params = PackageCreateTripParams.fromPackageSearchParams(Db.getPackageParams())
        if (params.isValid) {
            packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().tripParams.onNext(params)
        }
    }
}