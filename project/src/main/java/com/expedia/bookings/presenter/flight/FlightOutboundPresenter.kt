package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.RichContentUtils.getAmenitiesString
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import javax.inject.Inject

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    init {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    override fun back(): Boolean {
        if (isFlightGreedySearchEnabled(context)) {
            flightOfferViewModel.isGreedyCallAborted = true
            flightOfferViewModel.cancelGreedyCalls()
        }
        return super.back()
    }

    override fun setupComplete() {
        super.setupComplete()
        flightOfferViewModel.searchParamsObservable.subscribe {
            resultsPresenter.setLoadingState()
        }
        flightOfferViewModel.outboundResultsObservable.subscribe(resultsPresenter.resultsViewModel.flightResultsObservable)
        detailsPresenter.vm.selectedFlightClickedSubject.subscribe(flightOfferViewModel.confirmedOutboundFlightSelection)
        detailsPresenter.vm.selectedFlightLegSubject.subscribe(flightOfferViewModel.outboundSelected)
    }

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad(flight: FlightLeg) {
        FlightsV2Tracking.trackFlightOverview(true, flightOfferViewModel.tripTypeSearchSubject.value, flight, getAmenitiesString(context, flight))
    }

    override fun trackFlightSortFilterLoad() {
        FlightsV2Tracking.trackSortFilterClick()
    }

    override fun trackFlightScrollDepth(scrollDepth: Int) {
        FlightsV2Tracking.trackSRPScrollDepth(scrollDepth, true, flightOfferViewModel.tripTypeSearchSubject.value, flightOfferViewModel.totalOutboundResults)
    }

    override fun trackFlightResultsLoad() {
        val trackingData = searchTrackingBuilder.build()
        FlightsV2Tracking.trackResultOutBoundFlights(trackingData, flightOfferViewModel.isSubPub)
    }

    class FlightOutboundMissingTransitionException(exceptionMessage: String) : RuntimeException(exceptionMessage)
    override fun missingTransitionException(exceptionMessage: String): RuntimeException {
        return FlightOutboundMissingTransitionException(exceptionMessage)
    }
}
