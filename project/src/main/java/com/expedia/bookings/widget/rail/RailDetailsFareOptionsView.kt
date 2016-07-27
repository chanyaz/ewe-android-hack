package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
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

    private fun addFareOptionViews(offer: RailOffer) {
        if (offer.outboundLeg != null) {
            val outboundLegOption = offer.outboundLeg
            val offers = viewmodel.railResultsObservable.value.findOffersForLegOption(outboundLegOption)
            offers.forEach { offerForLeg ->
                val offerView = RailOfferView(context, offerForLeg,
                        viewmodel.offerSelectedObservable,
                        viewmodel.showAmenitiesObservable,
                        viewmodel.showFareRulesObservable)
                addView(offerView)
            }
        }
    }
}
