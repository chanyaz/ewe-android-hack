package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailFareOptionViewModel

class RailDetailsFareOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var viewmodel: RailDetailsViewModel by notNullAndObservable { vm ->
        vm.offerViewModel.offerSubject.subscribe {
            removeAllViews()
            addFareOptionViews(it)
        }
    }

    private fun addFareOptionViews(offer: RailOffer) {
        val outboundLegOption = offer.outboundLeg
        if (outboundLegOption != null) {
            val offers = viewmodel.railResultsObservable.value.findOffersForLegOption(outboundLegOption)
            offers.forEach { offerForLeg ->
                val fareOptionViewModel = RailFareOptionViewModel()
                val fareOptionView = RailFareOptionView(context)
                fareOptionView.viewModel = fareOptionViewModel

                fareOptionViewModel.showAmenitiesDetails.subscribe(viewmodel.showAmenitiesObservable)
                fareOptionViewModel.showFareDetails.subscribe(viewmodel.showFareRulesObservable)
                fareOptionViewModel.offerSelected.subscribe(viewmodel.offerSelectedObservable)
                fareOptionViewModel.offerFare.onNext(offerForLeg)

                addView(fareOptionView)
            }
        }
    }
}
