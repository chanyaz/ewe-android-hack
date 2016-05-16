package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSearchViewModel

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : FlightResultsPresenter(context, attrs) {

    override fun isOutboundResultsPresenter(): Boolean {
        return false
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }

    override fun trackFlightResultsLoad() {
    }
}
