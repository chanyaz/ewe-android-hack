package com.expedia.vm.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailFareOptionsViewModel(val showDeltaPricing: Boolean) {
    //inputs
    val railOffersAndInboundCheapestPricePairSubject = BehaviorSubject.create<Pair<List<RailSearchResponse.RailOffer>, Money?>>()

    //outputs
    val offerSelectedSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
    val showAmenitiesSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
    val showFareRulesSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
}