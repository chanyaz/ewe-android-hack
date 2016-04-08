package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    init {
        resultsPresenter.adapterPackage = FlightListAdapter(context, resultsPresenter.flightSelectedSubject)
        resultsPresenter.recyclerView.adapter = resultsPresenter.adapterPackage
        toolbarViewModel.isOutboundSearch.onNext(true)
    }

    override fun trackFlightOverviewLoad() {
    }
}

