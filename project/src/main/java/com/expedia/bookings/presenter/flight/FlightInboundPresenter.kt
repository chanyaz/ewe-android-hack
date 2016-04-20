package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    init {
        resultsPresenter.adapterPackage = FlightListAdapter(context, resultsPresenter.flightSelectedSubject)
        resultsPresenter.recyclerView.adapter = resultsPresenter.adapterPackage
        toolbarViewModel.isOutboundSearch.onNext(false)
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }
}

