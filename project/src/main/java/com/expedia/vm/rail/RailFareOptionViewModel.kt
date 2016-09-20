package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.PublishSubject

class RailFareOptionViewModel {
    val offerFare = PublishSubject.create<RailSearchResponse.RailOffer>()

    val showAmenitiesForFareClicked = PublishSubject.create<Unit>()
    val showFareRulesForFareClicked = PublishSubject.create<Unit>()
    val offerSelectButtonClicked = PublishSubject.create<Unit>()

    val priceObservable = offerFare.map { it.totalPrice.formattedPrice }
    val fareTitleObservable = offerFare.map { it.railProductList.first().aggregatedCarrierServiceClassDisplayName }
    val fareDescriptionObservable = offerFare.map { it.railProductList.first().aggregatedFareDescription }

    val showAmenitiesDetails = showAmenitiesForFareClicked.withLatestFrom(offerFare, { selected, offerFare -> offerFare })
    val showFareDetails = showFareRulesForFareClicked.withLatestFrom(offerFare, { selected, offerFare -> offerFare })
    val offerSelected = offerSelectButtonClicked.withLatestFrom(offerFare, { selected, offerFare -> offerFare })

}