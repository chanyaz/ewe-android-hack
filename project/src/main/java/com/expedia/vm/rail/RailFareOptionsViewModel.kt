package com.expedia.vm.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailOffer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailFareOptionsViewModel(val showDeltaPricing: Boolean) {
    //inputs
    val railOffersAndInboundCheapestPricePairSubject = BehaviorSubject.create<Pair<List<RailOffer>, Money?>>()

    //outputs
    val offerSelectedSubject = PublishSubject.create<RailOffer>()
    val showAmenitiesSubject = PublishSubject.create<RailOffer>()
    val showFareRulesSubject = PublishSubject.create<RailOffer>()
}