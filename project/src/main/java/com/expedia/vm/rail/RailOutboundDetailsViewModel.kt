package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.rail.RailConstants

class RailOutboundDetailsViewModel(context: Context) : BaseRailDetailsViewModel(context) {
    override fun getComparePrice(): Money? {
        val railSearchResponse = railResultsObservable.value

        if (railSearchResponse.hasInbound()) {
            return railSearchResponse.findLegWithBoundOrder(RailConstants.INBOUND_BOUND_ORDER)?.cheapestPrice
        } else return null
    }
}