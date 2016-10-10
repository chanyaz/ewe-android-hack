package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailFareOptionViewModel
import com.expedia.vm.rail.RailFareOptionsViewModel

class RailDetailsFareOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var viewModel: RailFareOptionsViewModel by notNullAndObservable { vm ->
        vm.railOffersPairSubject.subscribe { pair ->
            removeAllViews()
            addFareOptionViews(pair.first, pair.second)
        }
    }

    private fun addFareOptionViews(railOffers: List<RailSearchResponse.RailOffer>, comparePrice: Money?) {
        railOffers.forEach { offerForLeg ->
            val fareOptionViewModel = RailFareOptionViewModel()
            val fareOptionView = RailFareOptionView(context)
            fareOptionView.viewModel = fareOptionViewModel

            fareOptionViewModel.amenitiesSelectedObservable.subscribe(viewModel.showAmenitiesSubject)
            fareOptionViewModel.fareDetailsSelectedObservable.subscribe(viewModel.showFareRulesSubject)
            fareOptionViewModel.offerSelectedObservable.subscribe(viewModel.offerSelectedSubject)
            fareOptionViewModel.offerFareSubject.onNext(offerForLeg)
            fareOptionViewModel.cheapestPriceSubject.onNext(comparePrice)
            addView(fareOptionView)
        }
    }
}
