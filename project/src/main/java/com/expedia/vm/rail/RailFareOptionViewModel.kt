package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.rail.util.RailUtils
import com.expedia.util.Optional
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class RailFareOptionViewModel(val context: Context, val showDeltaPricing: Boolean) {
    val offerFareSubject = PublishSubject.create<RailOffer>()
    val inboundLegCheapestPriceSubject = PublishSubject.create<Optional<Money>>()

    val showAmenitiesForFareClicked = PublishSubject.create<Unit>()
    val showFareRulesForFareClicked = PublishSubject.create<Unit>()
    val offerSelectButtonClicked = PublishSubject.create<Unit>()
    val priceObservable = offerFareSubject.zipWith(inboundLegCheapestPriceSubject, { offer, cheapestPrice ->
        calculatePrice(offer, cheapestPrice.value)
    })
    val amenitiesSelectedObservable = showAmenitiesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val fareDetailsSelectedObservable = showFareRulesForFareClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })
    val offerSelectedObservable = offerSelectButtonClicked.withLatestFrom(offerFareSubject, { selected, offerFare -> offerFare })

    private val railProductObservable = offerFareSubject.map { offer -> offer.railProductList.first() }
    val fareTitleObservable = railProductObservable.map { railProduct ->
        "${railProduct.aggregatedCarrierServiceClassDisplayName} ${railProduct.aggregatedCarrierFareClassDisplayName}"
    }
    val fareDescriptionObservable = railProductObservable.map { railProduct -> railProduct.aggregatedFareDescription }
    val railCardAppliedObservable = railProductObservable.map { railProduct -> railProduct.hasRailCardApplied() }

    private fun calculatePrice(offer: RailOffer, inboundLegCheapestPrice: Money?): String {
        if (inboundLegCheapestPrice == null || offer.isOpenReturn && !showDeltaPricing) {
            return offer.totalPrice.formattedPrice
        }

        if (showDeltaPricing) {
            val priceDiff = RailUtils.subtractAndFormatMoney(offer.totalPrice, if (offer.isOpenReturn) offer.totalPrice else inboundLegCheapestPrice)
            return Phrase.from(context, R.string.rail_price_difference_TEMPLATE)
                    .put("pricedifference", priceDiff)
                    .format().toString()
        }

        return RailUtils.addAndFormatMoney(offer.totalPrice, inboundLegCheapestPrice)
    }
}