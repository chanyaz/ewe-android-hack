package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.utils.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class RailLegOptionViewModel(val context: Context) {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val cheapestLegPriceObservable = PublishSubject.create<Money?>()

    //Outputs
    val formattedStopsAndDurationObservable = legOptionObservable.map { legOption ->
        Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                .put("formattedchangecount", RailUtils.formatRailChangesText(context, legOption.noOfChanges)).format().toString()
    }
    val formattedTimeSubject = legOptionObservable.map { legOption ->
        DateTimeUtils.formatInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime()) }
    val aggregatedOperatingCarrierSubject = legOptionObservable.map { legOption -> legOption.aggregatedOperatingCarrier }
    val railCardAppliedObservable = legOptionObservable.map { legOption -> legOption.doesAnyOfferHasFareQualifier }

    val priceObservable = BehaviorSubject.create<String>()

    init {
        Observable.zip(legOptionObservable, cheapestLegPriceObservable, { legOption, cheapestOtherPrice ->
            calculatePrice(legOption, cheapestOtherPrice)
        }).subscribe(priceObservable)
    }

    //TODO for now we're just handling total pricing. We'll handle the delta pricing in the next story
    private fun calculatePrice(legOption: RailLegOption, cheapestPrice: Money?): String {
        if (cheapestPrice != null) {
            return RailUtils.addAndFormatMoney(legOption.bestPrice, cheapestPrice)
        } else {
            return legOption.bestPrice.formattedPrice
        }
    }
}