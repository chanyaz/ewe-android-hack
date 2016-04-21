package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    init {
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(false)
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }
}
