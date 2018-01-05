package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBackFlowFromOverviewEnabled
import com.expedia.bookings.utils.isFlexEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.vm.packages.PackageSearchType

class PackageActivity : AbstractAppCompatActivity() {
    var changedOutboundFlight = false

    private var isCrossSellPackageOnFSREnabled = false

    val packagePresenter by bindView<PackagePresenter>(R.id.package_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PackagesPageUsableData.SEARCH.pageUsableData.markPageLoadStarted()
        Ui.getApplication(this).defaultPackageComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.package_activity)
        Ui.showTransparentStatusBar(this)
        isCrossSellPackageOnFSREnabled = intent.getBooleanExtra(Constants.INTENT_PERFORM_HOTEL_SEARCH, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Constants.PACKAGE_PARAMS_NULL_RESTORE || Db.sharedInstance.getPackageParams() == null) {
            finish()
            return
        }
        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.collapseSelectedHotel()
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.collapseFlightDetails()
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.collapseFlightDetails()
        packagePresenter.bundleLoadingView.visibility = View.GONE

        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                val obj = packagePresenter.backStack.peek()
                if (Db.sharedInstance.packageParams.isChangePackageSearch() && obj !is Intent) {
                    onBackPressed()
                } else {
                    if (isCrossSellPackageOnFSREnabled) {
                        finish()
                        return
                    }

                    PackagesTracking().trackViewBundlePageLoad()

                    if (obj is Intent && obj.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
                        Db.sharedInstance.packageParams.currentFlights = Db.sharedInstance.packageParams.defaultFlights

                        //revert bundle view to be the state loaded outbound flights
                        packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectOutbound()
                        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)

                        val rate = Db.sharedInstance.packageSelectedRoom.rateInfo.chargeableRateInfo
                        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.setPriceValues(rate.packageTotalPrice, rate.packageSavings)

                    } else if (obj is Intent && obj.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT) && isBackFlowFromOverviewEnabled(this)) {
                        Db.sharedInstance.packageParams.currentFlights.set(1, Db.sharedInstance.packageParams.defaultFlights.get(1))

                        packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectInbound()
                        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)

                        packagePresenter.bundlePresenter.totalPriceWidget.resetPriceWidget()
                        val packagePrice = Db.sharedInstance.packageSelectedOutboundFlight.packageOfferModel.price
                        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.setPriceValues(packagePrice.packageTotalPrice, packagePrice.tripSavings)

                    } else if (packagePresenter.backStack.size == 2) {
                        Db.sharedInstance.packageParams.currentFlights = Db.sharedInstance.packageParams.defaultFlights

                        //revert bundle view to be the state loaded hotels
                        packagePresenter.bundlePresenter.totalPriceWidget.resetPriceWidget()
                        packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectHotel()
                        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
                    }
                }
                return
            }
            Constants.PACKAGE_API_ERROR_RESULT_CODE -> {
                val errorCode = (data?.extras?.getSerializable(Constants.PACKAGE_API_ERROR) as? PackageApiError.Code) ?: PackageApiError.Code.pkg_error_code_not_mapped
                packagePresenter.bundlePresenter.bundleWidget.viewModel.errorObservable.onNext(errorCode)
            }
        }

        when (requestCode) {
            Constants.HOTEL_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val errorString = data?.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
                    if (errorString != null) {
                        val errorCode = if (ApiError.Code.PACKAGE_SEARCH_ERROR.name == errorString) ApiError.Code.PACKAGE_SEARCH_ERROR else ApiError.Code.UNKNOWN_ERROR
                        packagePresenter.hotelOffersErrorObservable.onNext(errorCode)
                    } else {
                        //is is change hotel search, call createTrip, otherwise start outbound flight search
                        if (!Db.sharedInstance.packageParams.isChangePackageSearch()) {
                            PackagesPageUsableData.FLIGHT_OUTBOUND.pageUsableData.markPageLoadStarted()
                            packageFlightSearch()
                            val intent = Intent(this, PackageHotelActivity::class.java)
                            intent.putExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM, true)
                            intent.putExtra(Constants.REQUEST, Constants.HOTEL_REQUEST_CODE)
                            packagePresenter.backStack.push(intent)
                        } else {
                            packageCreateTrip()
                        }
                        packagePresenter.showBundleOverView()
                        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
                        if (!isMidAPIEnabled(this)) {
                            packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
                        }
                    }
                }
            }
            Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    PackagesPageUsableData.FLIGHT_INBOUND.pageUsableData.markPageLoadStarted()
                    packageFlightSearch()
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.onNext(Db.sharedInstance.packageSelectedOutboundFlight)

                    val intent = Intent(this, PackageFlightActivity::class.java)
                    intent.putExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT, true)
                    intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE)
                    packagePresenter.backStack.push(intent)

                    if (Db.sharedInstance.packageParams.isChangePackageSearch()) {
                        changedOutboundFlight = true
                    }
                    if (!isMidAPIEnabled(this)) {
                        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
                    }
                    packagePresenter.bundlePresenter.getCheckoutPresenter().getCheckoutViewModel().updateMayChargeFees(Db.sharedInstance.packageSelectedOutboundFlight)
                }
            }

            Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (!Db.sharedInstance.packageParams.isChangePackageSearch()) {
                        PackagesPageUsableData.RATE_DETAILS.pageUsableData.markPageLoadStarted()
                        val intent = Intent(this, PackageFlightActivity::class.java)
                        intent.putExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT, true)
                        intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE)
                        packagePresenter.backStack.push(intent)
                        packagePresenter.backStack.push(packagePresenter.bundlePresenter)
                        packagePresenter.bundlePresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
                    } else {
                        //If change flight, remove changed outbound flight intent
                        packagePresenter.backStack.pop()
                    }
                    packagePresenter.bundlePresenter.getCheckoutPresenter().toolbarDropShadow.visibility = View.GONE

                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageFlightBundle().second)

                    packageCreateTrip()
                    packagePresenter.showBundleOverView()
                    if (!isMidAPIEnabled(this)) {
                        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
                    }
                    packagePresenter.bundlePresenter.setToolbarNavIcon(false)
                    packagePresenter.bundlePresenter.getCheckoutPresenter().getCheckoutViewModel().updateMayChargeFees(Db.getPackageFlightBundle().second)
                }
            }
        }
    }

    override fun onBackPressed() {
        //for change package path
        if (packagePresenter.backStack.peek() is PackageOverviewPresenter && Db.sharedInstance.packageParams?.isChangePackageSearch() ?: false) {
            if (changedOutboundFlight) {
                changedOutboundFlight = false
                val outbound = Db.getPackageFlightBundle().first
                val inbound = Db.getPackageFlightBundle().second
                Db.sharedInstance.packageParams.packagePIID = inbound.packageOfferModel.piid
                Db.sharedInstance.packageParams.currentFlights[0] = outbound.legId
                Db.setPackageSelectedOutboundFlight(outbound)
                packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.onNext(outbound)
            }
            packagePresenter.bundlePresenter.bundleWidget.viewModel.cancelSearchObservable.onNext(Unit)
            packageCreateTrip()
            packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
            packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            return
        }

        if (!packagePresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        Events.post(Events.AppBackgroundedOnResume())
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        }
        else {
            Ui.hideKeyboard(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Db.getTripBucket().clearPackages()
    }

    private fun packageFlightSearch() {
        PackagesTracking().trackViewBundlePageLoad()
        packagePresenter.bundlePresenter.bundleWidget.viewModel.flightParamsObservable.onNext(Db.sharedInstance.packageParams)
    }

    @VisibleForTesting( otherwise = VisibleForTesting.PRIVATE)
    fun packageCreateTrip() {
        packagePresenter.bundleLoadingView.visibility = View.GONE
        Db.sharedInstance.packageParams.pageType = null
        changedOutboundFlight = false

        if (isMidAPIEnabled(this)) {
            packagePresenter.bundlePresenter.performMIDCreateTripSubject.onNext(Unit)
        } else {
            val params = PackageCreateTripParams.fromPackageSearchParams(Db.sharedInstance.packageParams)
            if (params.isValid) {
                getCreateTripViewModel().reset()
                params.flexEnabled = isFlexEnabled()
                getCreateTripViewModel().tripParams.onNext(params)
            }
        }
    }

    @VisibleForTesting( otherwise = VisibleForTesting.PRIVATE)
    fun getCreateTripViewModel() = packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel()
}
