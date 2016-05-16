package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.tracking.PackagesTracking
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

    init {
        val bestPlusAllFlights = Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch() && it.packageOfferModel != null }

        // move bestFlight to the first place of the list
        val bestFlight = bestPlusAllFlights.find { it.isBestFlight }
        var allFlights = bestPlusAllFlights.filterNot { it.isBestFlight }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount }.toMutableList()

        allFlights.add(0, bestFlight)
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.getPackageParams().isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)
        resultsPresenter.resultsViewModel.flightResultsObservable.onNext(allFlights)
        if (!isOutboundSearch() && Db.getPackageSelectedOutboundFlight() != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.getPackageSelectedOutboundFlight())
        }
        overviewPresenter.vm.selectedFlightClicked.subscribe(flightOverviewSelected)
        var cityBound: String = if (isOutboundSearch()) Db.getPackageParams().destination.regionNames.shortName else Db.getPackageParams().origin.regionNames.shortName
        val numTravelers = Db.getPackageParams().guests
        toolbarViewModel.isOutboundSearch.onNext(isOutboundSearch())
        toolbarViewModel.city.onNext(cityBound)
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundSearch()) Db.getPackageParams().checkIn else Db.getPackageParams().checkOut)
        trackFlightResultsLoad()
    }

    fun isOutboundSearch():Boolean = Db.getPackageParams()?.isOutboundSearch() ?: false

    override fun isOutboundResultsPresenter(): Boolean = isOutboundSearch()

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
}

