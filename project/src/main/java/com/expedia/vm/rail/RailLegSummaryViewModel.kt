package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.widget.RailViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailLegSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailOffer>()

    //outputs
    val operatorObservable = BehaviorSubject.create<String>()
    val formattedStopsAndDurationObservable = BehaviorSubject.create<String>()
    val formattedTimesObservable = BehaviorSubject.create<String>()
    val legOptionObservable = BehaviorSubject.create<RailLegOption>()
    val selectedRailOfferObservable = BehaviorSubject.create<RailOffer>()
    val fareDescriptionLabelObservable = BehaviorSubject.create<String>()
    val showLegInfoObservable = PublishSubject.create<Unit>()

    init {
        railOfferObserver.subscribe {
            val legOption: RailLegOption = it.outboundLeg!!

            val formattedStopsAndDuration = Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                    .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                    .put("formattedchangecount", RailViewModel.formatChangesText(context, legOption.noOfChanges)).format().toString()
            formattedStopsAndDurationObservable.onNext(formattedStopsAndDuration)

            val formattedTimes = DateTimeUtils.formatInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
            formattedTimesObservable.onNext(formattedTimes.toString())
            operatorObservable.onNext(legOption.allOperators())
            legOptionObservable.onNext(legOption)
            selectedRailOfferObservable.onNext(it)
            fareDescriptionLabelObservable.onNext(it.railProductList.first().aggregatedFareDescription)
        }
    }
}
