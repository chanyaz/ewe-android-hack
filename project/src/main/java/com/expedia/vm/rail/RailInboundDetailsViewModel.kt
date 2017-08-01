package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.rail.responses.RailOffer
import io.reactivex.subjects.BehaviorSubject

class RailInboundDetailsViewModel(context: Context) : RailDetailsViewModel(context) {

    val selectedOutboundOfferSubject = BehaviorSubject.create<RailOffer>()

    override fun filterFareOptions(railOffers: List<RailOffer>): List<RailOffer> {
        val selectedOutboundOffer = getSelectedOutboundOffer()
        if (selectedOutboundOffer != null) {
            val railSearchResponse = railResultsObservable.value
            // filter inbound offers based on outbound offer selected
            return railSearchResponse.filterInboundOffers(railOffers, selectedOutboundOffer)
        } else return emptyList()
    }

    private fun getSelectedOutboundOffer(): RailOffer? {
        return selectedOutboundOfferSubject?.value
    }
}
