package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.tracking.FlightsV2Tracking

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    override fun back(): Boolean {
        flightOfferViewModel.cancelSearchObservable.onNext(Unit)
        return super.back()
    }

    override fun setupComplete() {
        super.setupComplete()
        flightOfferViewModel.outboundResultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOfferViewModel.confirmedOutboundFlightSelection)
        overviewPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.outboundSelected)
        resultsPresenter.setLoadingState()
        showResults()
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad() {
        FlightsV2Tracking.trackFlightOverview(true)
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightResultsLoad() {
        val flightLegs = flightOfferViewModel.outboundResultsObservable.value
        FlightsV2Tracking.trackResultOutBoundFlights(Db.getFlightSearchParams(), flightLegs)
    }
}
