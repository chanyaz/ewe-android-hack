package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.tracking.FlightsV2Tracking

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : AbstractMaterialFlightResultsPresenter(context, attrs) {

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
        val flightSearchParams = this.flightSearchViewModel.searchParamsObservable.value
        FlightsV2Tracking.trackResultOutBoundFlights(flightSearchParams)
    }
}
