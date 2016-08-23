package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.tracking.FlightsV2Tracking

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    override fun setupComplete() {
        super.setupComplete()
        flightOfferViewModel.confirmedOutboundFlightSelection.subscribe(resultsPresenter.outboundFlightSelectedSubject)
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOfferViewModel.confirmedInboundFlightSelection)
        overviewPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.inboundSelected)
        flightOfferViewModel.inboundResultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return false
    }

    override fun trackFlightOverviewLoad() {
        FlightsV2Tracking.trackFlightOverview(false)
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightResultsLoad() {
        FlightsV2Tracking.trackResultInBoundFlights()
    }

}
