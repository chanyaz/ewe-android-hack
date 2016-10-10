package com.expedia.vm.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.rail.RailUtils
import rx.subjects.PublishSubject

class RailFareOptionViewModel {
    val offerFareSubject = PublishSubject.create<RailSearchResponse.RailOffer>()
    val cheapestPriceSubject = PublishSubject.create<Money?>()

    val showAmenitiesForFareClicked = PublishSubject.create<Unit>()
    val showFareRulesForFareClicked = PublishSubject.create<Unit>()
    val offerSelectButtonClicked = PublishSubject.create<Unit>()
    val priceObservable = offerFareSubject.zipWith(cheapestPriceSubject, { offer, cheapestPrice ->
        calculatePrice(offer, cheapestPrice)
    })

    val fareTitleObservable = offerFareSubject.map { offer -> offer.railProductList.first().aggregatedCarrierFareClassDisplayName }
    val fareDescriptionObservable = offerFareSubject.map { offer -> offer.railProductList.first().aggregatedFareDescription }
    val railCardAppliedObservable = offerFareSubject.map { offer -> offer.hasRailCardApplied() }
    val amenitiesSelectedObservable = showAmenitiesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val fareDetailsSelectedObservable = showFareRulesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val offerSelectedObservable = offerSelectButtonClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })

    //TODO for now we're just handling total pricing. We'll handle the delta pricing in the next story
    private fun calculatePrice(offer: RailSearchResponse.RailOffer, cheapestPrice: Money?): String {
        if (cheapestPrice != null) {
            return RailUtils.addAndFormatMoney(offer.totalPrice, cheapestPrice)
        } else {
            return offer.totalPrice.formattedPrice
        }
    }
}