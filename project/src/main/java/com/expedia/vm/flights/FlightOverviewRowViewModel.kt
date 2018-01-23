package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg

class FlightOverviewRowViewModel(context: Context, flightLeg: FlightLeg) : FlightViewModel(context, flightLeg) {

    override fun appendAccessibilityContentDescription(): String {
        return context.getString(R.string.row_expand_button_description)
    }

    override fun getFlightDetailCardContDescriptionStringID(): Int {
        return R.string.flight_detail_card_cont_desc_without_price_TEMPLATE
    }
}
