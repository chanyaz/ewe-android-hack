package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailFareOptionsViewModel
import com.expedia.vm.rail.RailFareOptionViewModel

class RailDetailsFareOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var viewModel: RailFareOptionsViewModel by notNullAndObservable { vm ->
        vm.railOffersSubject.subscribe { railOffers ->
            removeAllViews()
            addFareOptionViews(railOffers)
        }
    }

    private fun addFareOptionViews(railOffers: List<RailSearchResponse.RailOffer>) {
        railOffers.forEach { offerForLeg ->
            val fareOptionViewModel = RailFareOptionViewModel()
            val fareOptionView = RailFareOptionView(context)
            fareOptionView.viewModel = fareOptionViewModel

            fareOptionViewModel.amenitiesSelectedObservable.subscribe(viewModel.showAmenitiesSubject)
            fareOptionViewModel.fareDetailsSelectedObservable.subscribe(viewModel.showFareRulesSubject)
            fareOptionViewModel.offerSelectedObservable.subscribe(viewModel.offerSelectedSubject)
            fareOptionViewModel.offerFareSubject.onNext(offerForLeg)

            addView(fareOptionView)
        }
    }
}
