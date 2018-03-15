package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailFareOptionViewModel
import com.expedia.vm.rail.RailFareOptionsViewModel

class RailDetailsFareOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var viewModel: RailFareOptionsViewModel by notNullAndObservable { vm ->
        vm.railOffersAndInboundCheapestPricePairSubject.subscribe { pair ->
            removeAllViews()
            addFareOptionViews(pair.first, pair.second, vm.showDeltaPricing)
        }
    }

    private fun addFareOptionViews(
        railOffers: List<RailOffer>,
        inboundLegCheapestPrice: Money?,
        showDeltaPricing: Boolean
    ) {
        railOffers.forEach { offerForLeg ->
            val fareOptionViewModel = RailFareOptionViewModel(context, showDeltaPricing)
            val fareOptionView = RailFareOptionView(context)
            fareOptionView.viewModel = fareOptionViewModel

            fareOptionViewModel.amenitiesSelectedObservable.subscribe(viewModel.showAmenitiesSubject)
            fareOptionViewModel.fareDetailsSelectedObservable.subscribe(viewModel.showFareRulesSubject)
            fareOptionViewModel.offerSelectedObservable.subscribe(viewModel.offerSelectedSubject)
            fareOptionViewModel.offerFareSubject.onNext(offerForLeg)
            fareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Optional(inboundLegCheapestPrice))
            addView(fareOptionView)
        }
    }
}
