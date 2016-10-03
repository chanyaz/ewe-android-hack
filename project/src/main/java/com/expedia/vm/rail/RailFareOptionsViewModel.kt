package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailFareOptionsViewModel {
    //inputs
    val railOffersSubject = BehaviorSubject.create<List<RailSearchResponse.RailOffer>>()

    //outputs
    val offerSelectedSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
    val showAmenitiesSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
    val showFareRulesSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
}