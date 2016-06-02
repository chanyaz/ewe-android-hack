package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.tracking.FlightsV2Tracking

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {

    override fun isOutboundResultsPresenter(): Boolean {
        return true
    }

    override fun trackFlightOverviewLoad() {
    }

    override fun trackFlightSortFilterLoad() {
    }

    override fun trackFlightResultsLoad() {
    }

    override fun trackShowBaggageFee() = FlightsV2Tracking.trackFlightBaggageFeeClick()

    override fun trackShowPaymentFees() {
        // not applicable to outbound flight presenter
    }

    override fun setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.flights_toolbar_menu)
    }
}
