package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.tracking.FlightsV2Tracking

class FlightInboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    override fun isOutboundResultsPresenter(): Boolean {
        return false
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }

    override fun trackFlightResultsLoad() {
    }

    override fun trackShowBaggageFee() = FlightsV2Tracking.trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() = FlightsV2Tracking.trackPaymentFeesClick()

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.flights_toolbar_menu)
    }
}
