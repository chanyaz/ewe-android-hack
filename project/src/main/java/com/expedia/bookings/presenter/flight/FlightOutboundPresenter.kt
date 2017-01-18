package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import javax.inject.Inject

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    init {
        Ui.getApplication(context).flightComponent().inject(this)
    }
    override fun back(): Boolean {
        flightOfferViewModel.cancelSearchObservable.onNext(Unit)
        return super.back()
    }

    override fun setupComplete() {
        super.setupComplete()
        flightOfferViewModel.outboundResultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)
        overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOfferViewModel.confirmedOutboundFlightSelection)
        overviewPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.outboundSelected)
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad() {
        val isRoundTrip = flightOfferViewModel.isRoundTripSearchSubject.value
        FlightsV2Tracking.trackFlightOverview(true, isRoundTrip)
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightResultsLoad() {
        if (searchTrackingBuilder.isWorkComplete()) {
            val trackingData = searchTrackingBuilder.build()
            FlightsV2Tracking.trackResultOutBoundFlights(trackingData)
        }
    }
}
