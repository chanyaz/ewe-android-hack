package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailDetailsViewModel(val context: Context) {
    val offerViewModel = RailOfferViewModel(context)
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()

    val offerSelectedObservable = PublishSubject.create<RailOffer>()
    val showAmenitiesObservable = PublishSubject.create<RailOffer>()
    val showFareRulesObservable = PublishSubject.create<RailOffer>()
}

