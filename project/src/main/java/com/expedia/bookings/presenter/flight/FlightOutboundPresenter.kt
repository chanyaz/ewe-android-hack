package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSearchViewModel

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : FlightResultsPresenter(context, attrs) {

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }

    override fun trackFlightResultsLoad() {
    }
}

