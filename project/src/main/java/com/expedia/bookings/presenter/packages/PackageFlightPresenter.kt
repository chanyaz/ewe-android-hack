package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.ActionMenuItemView
import android.util.AttributeSet
import com.expedia.bookings.R
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.widget.PackageFlightListAdapter
import com.expedia.util.endlessObserver

class PackageFlightPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    private val flightOverviewSelected = endlessObserver<FlightLeg> { flight ->
        val params = Db.getPackageParams()
        if (flight.outbound) {
            Db.setPackageSelectedOutboundFlight(flight)
            params.currentFlights[0] = flight.legId
        } else {
            Db.setPackageSelectedInboundFlight(flight)
            params.currentFlights[1] = flight.legId
        }
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid

        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    override fun addResultOverViewTransition() {
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.getPackageSelectedOutboundFlight())
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            addBackFlowTransition()
            selectedFlightResults.onNext(Db.getPackageSelectedInboundFlight())
        } else {
            super.addResultOverViewTransition()
        }
    }

    private fun addBackFlowTransition() {
        addDefaultTransition(backFlowDefaultTransition)
        addTransition(backFlowOverviewTransition)
        show(resultsPresenter)
        show(overviewPresenter)
    }

    init {
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            val params = Db.getPackageParams()
            params.selectedLegId = null
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE))
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            Db.setPackageResponse(PackageResponseUtils.loadPackageResponse(context, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE))
        }

        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch() ?: false
        val bestPlusAllFlights = Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch && it.packageOfferModel != null }

        // move bestFlight to the first place of the list
        val bestFlight = bestPlusAllFlights.find { it.isBestFlight }
        var allFlights = bestPlusAllFlights.filterNot { it.isBestFlight }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount }.toMutableList()

        allFlights.add(0, bestFlight)
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.getPackageParams().isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)
        resultsPresenter.resultsViewModel.flightResultsObservable.onNext(allFlights)
        if (!isOutboundResultsPresenter() && Db.getPackageSelectedOutboundFlight() != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.getPackageSelectedOutboundFlight())
        }
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOverviewSelected)
        var cityBound: String = if (isOutboundResultsPresenter()) Db.getPackageParams().destination.regionNames.shortName else Db.getPackageParams().origin.regionNames.shortName
        val numTravelers = Db.getPackageParams().guests
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        toolbarViewModel.city.onNext(cityBound)
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundResultsPresenter()) Db.getPackageParams().checkIn else Db.getPackageParams().checkOut)
        trackFlightResultsLoad()
    }

    override fun isOutboundResultsPresenter(): Boolean = Db.getPackageParams()?.isOutboundSearch() ?: false

    override fun trackFlightOverviewLoad() {
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch() ?: false
        PackagesTracking().trackFlightRoundTripDetailsLoad(isOutboundSearch)
    }

    override fun trackFlightSortFilterLoad() {
        PackagesTracking().trackFlightSortFilterLoad()
    }

    override fun trackFlightResultsLoad() {
        PackagesTracking().trackFlightRoundTripLoad(Db.getPackageParams()?.isOutboundSearch() ?: false)
    }

    private val backFlowDefaultTransition = object : DefaultTransition(FlightResultsListViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            resultsPresenter.visibility = View.INVISIBLE
            overviewPresenter.visibility = View.VISIBLE
        }
    }

    private val backFlowOverviewTransition = object : Transition(FlightResultsListViewPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(!forward)
            overviewPresenter.visibility = if (forward) View.VISIBLE else View.INVISIBLE
            resultsPresenter.visibility = if (forward) View.INVISIBLE else View.VISIBLE

            if (!forward) {
                trackFlightResultsLoad()
            }
        }
    }

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.package_flights_menu)
        menuFilter = toolbar.findViewById(R.id.menu_filter) as ActionMenuItemView
        menuFilter!!.setOnClickListener { show(filter) }
    }

    override fun trackShowBaggageFee() = PackagesTracking().trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() {
        // do nothing. Not applicable to Packages LOB
    }

    override fun shouldShowBundlePrice(): Boolean {
        return true
    }
}
