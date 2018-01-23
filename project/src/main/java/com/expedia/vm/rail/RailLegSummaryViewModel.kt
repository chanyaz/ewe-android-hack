package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.rail.util.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailLegSummaryViewModel(context: Context) {
    //inputs
    val railLegOptionObserver = BehaviorSubject.create<RailLegOption>()
    val railProductObserver = BehaviorSubject.create<RailProduct>()

    //outputs
    val operatorObservable = PublishSubject.create<String>()
    val formattedStopsAndDurationObservable = PublishSubject.create<String>()
    val formattedTimesObservable = PublishSubject.create<String>()
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val showLegInfoObservable = PublishSubject.create<Unit>()
    val overtakenSubject = PublishSubject.create<Boolean>()

    val fareDescriptionObservable = railProductObserver.map { railProduct -> railProduct.aggregatedFareDescription }
    val railCardNameObservable = railProductObserver.map { railProduct ->
        getAppliedRailcardNames(railProduct)
    }

    val railSummaryContentDescription = ObservableOld.combineLatest(railLegOptionObserver, formattedStopsAndDurationObservable) { legOption, stopsAndDuration ->
        Phrase.from(context, R.string.rail_result_card_cont_desc_TEMPLATE)
                .put("departuretime", RailUtils.formatTimeToDeviceFormat(context, legOption.getDepartureDateTime()))
                .put("arrivaltime", RailUtils.formatTimeToDeviceFormat(context, legOption.getArrivalDateTime()))
                .put("trainoperator", legOption.aggregatedOperatingCarrier)
                .put("tripdurationandchanges", stopsAndDuration)
                .format().toString()
    }

    init {
        railLegOptionObserver.subscribe { legOption ->
            val formattedStopsAndDuration = Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                    .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                    .put("formattedchangecount", RailUtils.formatRailChangesText(context, legOption.noOfChanges)).format().toString()
            formattedStopsAndDurationObservable.onNext(formattedStopsAndDuration)

            val formattedTimes = DateTimeUtils.formatInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
            formattedTimesObservable.onNext(formattedTimes.toString())
            operatorObservable.onNext(legOption.aggregatedOperatingCarrier)
            legOptionObservable.onNext(legOption)
            overtakenSubject.onNext(legOption.overtakenJourney)
        }
    }

    private fun getAppliedRailcardNames(railProduct: RailProduct): String {
        if (railProduct.hasRailCardApplied()) {
            return railProduct.fareQualifierList.map { railCard -> railCard.name }.joinToString()
        }
        return ""
    }
}
