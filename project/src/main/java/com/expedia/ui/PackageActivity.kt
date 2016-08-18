package com.expedia.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.util.TypedValue
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.PackageSearchType

class PackageActivity : AbstractAppCompatActivity() {

    var changedOutboundFlight = false

    val packagePresenter: PackagePresenter by lazy {
        findViewById(R.id.package_presenter) as PackagePresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultPackageComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.package_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.collapseSelectedHotel()
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.collapseFlightDetails()
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.collapseFlightDetails()

        if (resultCode == Activity.RESULT_CANCELED) {
            val obj = packagePresenter.backStack.peek()
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

                    val rate = Db.getPackageSelectedOutboundFlight().packageOfferModel.price
                    packagePresenter.bundlePresenter.getCheckoutPresenter().totalPriceWidget.viewModel.setPriceValues(rate.packageTotalPrice, rate.tripSavings)

                } else if (obj is Intent && obj.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
                    Db.getPackageParams().currentFlights = Db.getPackageParams().defaultFlights

                    //revert bundle view to be the state loaded outbound flights
                    packagePresenter.bundlePresenter.bundleWidget.revertBundleViewToSelectOutbound()
                    packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)

                    val rate = Db.getPackageSelectedRoom().rateInfo.chargeableRateInfo
                    packagePresenter.bundlePresenter.getCheckoutPresenter().totalPriceWidget.viewModel.setPriceValues(rate.packageTotalPrice, rate.packageSavings)

                } else if (packagePresenter.backStack.size == 2) {
                    Db.getPackageParams().currentFlights = Db.getPackageParams().defaultFlights

                    //revert bundle view to be the state loaded hotels
                    packagePresenter.bundlePresenter.getCheckoutPresenter().totalPriceWidget.resetPriceWidget()
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
                        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
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
                    packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
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
                    packagePresenter.bundlePresenter.getCheckoutPresenter().toolbarDropShadow.visibility = View.GONE

                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
                    packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageFlightBundle().second)
                    packageCreateTrip()
                    packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)
                }
            }
        }
    }

    override fun onBackPressed() {
        packagePresenter.bundlePresenter.bundleWidget.viewModel.cancelSearchObservable.onNext(Unit)

        //for change package path
        if (packagePresenter.backStack.peek() is PackageOverviewPresenter && Db.getPackageParams()?.isChangePackageSearch() ?: false) {
            if (changedOutboundFlight) {
                changedOutboundFlight = false
                val outbound = Db.getPackageFlightBundle().first
                val inbound = Db.getPackageFlightBundle().second
                Db.getPackageParams().packagePIID = inbound.packageOfferModel.piid
                Db.getPackageParams().currentFlights[0] = outbound.legId
                Db.setPackageSelectedOutboundFlight(outbound)
                packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.onNext(outbound)
            }
            packageCreateTrip()
            packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
            packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            return
        }
        if (packagePresenter.backStack.size > 2 && packagePresenter.backStack.peek() is PackageOverviewPresenter) {
            val currentState = (packagePresenter.backStack.peek() as PackageOverviewPresenter).currentState
            if (currentState == BaseOverviewPresenter.BundleDefault::class.java.name) {
                showBackToSearchDialog()
                return
            }
        }
        if (!packagePresenter.back()) {
            super.onBackPressed()
        }
    }

    private fun showBackToSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.package_checkout_back_dialog_title)
        builder.setMessage(R.string.package_checkout_back_dialog_message)
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(getString(R.string.start_over)) { dialog, which ->
            packagePresenter.show(packagePresenter.searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }
        val dialog = builder.create()
        dialog.show()
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
    }

    private fun packageFlightSearch() {
        PackagesTracking().trackViewBundlePageLoad()
        packagePresenter.bundlePresenter.bundleWidget.viewModel.flightParamsObservable.onNext(Db.getPackageParams())
    }

    private fun packageCreateTrip() {
        Db.getPackageParams().pageType = null
        changedOutboundFlight = false
        val params = PackageCreateTripParams.fromPackageSearchParams(Db.getPackageParams())
        if (params.isValid) {
            packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().tripParams.onNext(params)
        }
    }
}