package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.utils.rail.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailLegSummaryViewModel(context: Context) {
    //inputs
    val railLegOptionObserver = BehaviorSubject.create<RailLegOption>()

    //outputs
    val operatorObservable = PublishSubject.create<String>()
    val formattedStopsAndDurationObservable = PublishSubject.create<String>()
    val formattedTimesObservable = PublishSubject.create<String>()
    val legOptionObservable = PublishSubject.create<RailLegOption>()
    val fareDescriptionLabelObservable = PublishSubject.create<String>()
    val showLegInfoObservable = PublishSubject.create<Unit>()
    val overtakenSubject = PublishSubject.create<Boolean>()
    val railCardAppliedNameSubject = PublishSubject.create<String>()

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
}
