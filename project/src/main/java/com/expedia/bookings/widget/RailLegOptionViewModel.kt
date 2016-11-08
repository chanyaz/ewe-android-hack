package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.utils.rail.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject

open class RailLegOptionViewModel(val context: Context, val inbound: Boolean) {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val cheapestLegPriceObservable = PublishSubject.create<Money?>()
    val offerSubject = PublishSubject.create<RailOffer>()

    //Outputs
    val formattedStopsAndDurationObservable = legOptionObservable.map { legOption ->
        Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                .put("formattedchangecount", RailUtils.formatRailChangesText(context, legOption.noOfChanges)).format().toString()
    }
    val formattedTimeSubject = legOptionObservable.map { legOption ->
        RailUtils.formatTimeInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
    }
    val aggregatedOperatingCarrierSubject = legOptionObservable.map { legOption -> legOption.aggregatedOperatingCarrier }
    val railCardAppliedObservable = legOptionObservable.map { legOption -> legOption.doesAnyOfferHasFareQualifier }
    val priceObservable = Observable.combineLatest(legOptionObservable, cheapestLegPriceObservable, offerSubject, { legOption, cheapestPrice, offer ->
        calculatePrice(legOption, cheapestPrice, offer)
    })

    private fun calculatePrice(legOption: RailLegOption, cheapestPrice: Money?, offer: RailOffer?): String {
        if (cheapestPrice == null) {
            return legOption.bestPrice.formattedPrice
        }

        if (inbound) {
            val priceDiff = getPriceDiff(legOption, cheapestPrice, offer)
            return Phrase.from(context, R.string.rail_price_difference_TEMPLATE)
                    .put("pricedifference", priceDiff)
                    .format().toString()
        }
        return RailUtils.addAndFormatMoney(legOption.bestPrice, cheapestPrice)
    }

    private fun getPriceDiff(legOption: RailLegOption, cheapestPrice: Money, offer: RailOffer?): String {
        if (offer != null && offer.isOpenReturn) {
            return RailUtils.subtractAndFormatMoney(offer.totalPrice, offer.totalPrice)
        }
        return RailUtils.subtractAndFormatMoney(legOption.bestPrice, cheapestPrice)
    }
}