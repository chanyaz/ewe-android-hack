package com.expedia.bookings.rail.widget

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.rail.util.RailUtils
import com.expedia.util.Optional
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

open class RailLegOptionViewModel(val context: Context, val inbound: Boolean) {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val cheapestLegPriceObservable = PublishSubject.create<Optional<Money>>()
    val offerSubject = PublishSubject.create<Optional<RailOffer>>()

    //Outputs
    val formattedStopsAndDurationObservable = legOptionObservable.map { legOption ->
        Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                .put("formattedchangecount", RailUtils.formatRailChangesText(context, legOption.noOfChanges)).format().toString()
    }
    val formattedTimeSubject = legOptionObservable.map { legOption ->
        RailUtils.formatTimeIntervalToDeviceFormat(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
    }
    val aggregatedOperatingCarrierSubject = legOptionObservable.map { legOption -> legOption.aggregatedOperatingCarrier }
    val railCardAppliedObservable = legOptionObservable.map { legOption -> legOption.doesAnyOfferHasFareQualifier }
    val priceObservable = ObservableOld.combineLatest(legOptionObservable, cheapestLegPriceObservable, offerSubject, { legOption, cheapestPrice, offer ->
        calculatePrice(legOption, cheapestPrice.value, offer.value)
    })

    val contentDescriptionObservable = ObservableOld.combineLatest(legOptionObservable, priceObservable, formattedStopsAndDurationObservable, { legOption, price, stopsAndDuration ->
        getContentDescription(legOption, price, stopsAndDuration)
    })

    open fun getContentDescription(legOption: RailLegOption, price: String, stopsAndDuration: String): String {
        val result = StringBuffer()
        result.append(Phrase.from(context, R.string.rail_result_card_cont_desc_TEMPLATE)
                .put("departuretime", RailUtils.formatTimeToDeviceFormat(context, legOption.getDepartureDateTime()))
                .put("arrivaltime", RailUtils.formatTimeToDeviceFormat(context, legOption.getArrivalDateTime()))
                .put("trainoperator", legOption.aggregatedOperatingCarrier)
                .put("tripdurationandchanges", stopsAndDuration)
                .format()
                .toString())

        if (legOption.doesAnyOfferHasFareQualifier) {
            result.append(" ").append(context.getString(R.string.rail_railcard_applied_cont_desc))
        }

        result.append(" ").append(Phrase.from(context, R.string.rail_result_card_price_from_cont_desc_TEMPLATE)
                .put("price", price).format().toString())

        return result.toString()
    }

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