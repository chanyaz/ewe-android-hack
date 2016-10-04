package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.PublishSubject

class RailFareOptionViewModel {
    val offerFareSubject = PublishSubject.create<RailSearchResponse.RailOffer>()

    val showAmenitiesForFareClicked = PublishSubject.create<Unit>()
    val showFareRulesForFareClicked = PublishSubject.create<Unit>()
    val offerSelectButtonClicked = PublishSubject.create<Unit>()

    val priceObservable = offerFareSubject.map { offer ->
        offer.totalPrice.formattedPrice
    }
    val fareTitleObservable = offerFareSubject.map { offer ->
        offer.railProductList.first().aggregatedCarrierFareClassDisplayName
    }
    val fareDescriptionObservable = offerFareSubject.map { offer ->
        offer.railProductList.first().aggregatedFareDescription
    }

    val amenitiesSelectedObservable = showAmenitiesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val fareDetailsSelectedObservable = showFareRulesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val offerSelectedObservable = offerSelectButtonClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
}