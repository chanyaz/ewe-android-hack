package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {
    init {
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(true)
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }

    override fun trackFlightResultsLoad() {
    }
}

