package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailDetailsViewModel

class RailDetailsFareOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    var viewmodel: RailDetailsViewModel by notNullAndObservable { vm ->
        vm.offerViewModel.offerSubject.subscribe {
            removeAllViews()
            addFareOptionViews(it)
        }
    }

    private fun addFareOptionViews(offer: RailSearchResponse.RailOffer) {
        if (offer.outboundLeg != null) {
            val offers = viewmodel.railResultsObservable.value.findOffersForLegOption(offer.outboundLeg?.legOptionIndex)
            offers.forEach {
                val offerView = RailOfferView(context, it, viewmodel.offerSelectedObservable)
                addView(offerView)
            }
        }
    }
}
