package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

open class RailLegOptionViewModel(val context: Context) {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val legObservable = PublishSubject.create<RailSearchResponse.RailLeg>()

    //Outputs
    val formattedStopsAndDurationObservable = legOptionObservable.map { legOption ->
        Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                .put("formattedchangecount", RailUtils.formatRailChangesText(context, legOption.noOfChanges)).format().toString()
    }
    val formattedTimeSubject = legOptionObservable.map { legOption ->
        DateTimeUtils.formatInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
    }
    val aggregatedOperatingCarrierSubject = legOptionObservable.map { legOption -> legOption.aggregatedOperatingCarrier }
    val railCardAppliedObservable = legOptionObservable.map { legOption -> legOption.doesAnyOfferHasFareQualifier }

    val priceObservable = legObservable.zipWith(legOptionObservable, { leg, legOption ->
        calculatePrice(legOption, leg)
    })

    private fun calculatePrice(legOption: RailLegOption, leg: RailSearchResponse.RailLeg): String {
        val inbound = leg.legBoundOrder == 2

        if (inbound) {
            val priceDiff = RailUtils.subtractAndFormatMoney(legOption.bestPrice, leg.cheapestPrice)
            return Phrase.from(context, R.string.rail_price_difference_TEMPLATE)
                    .put("pricedifference", priceDiff)
                    .format().toString()
        }

        val cheapestPrice = leg.cheapestInboundPrice
        if (cheapestPrice != null) {
            return RailUtils.addAndFormatMoney(legOption.bestPrice, cheapestPrice)
        }

        return legOption.bestPrice.formattedPrice
    }
}