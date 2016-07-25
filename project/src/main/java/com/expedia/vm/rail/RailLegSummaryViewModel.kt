package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.rail.responses.LegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.widget.RailViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class RailLegSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailSearchResponse.RailOffer>()

    //outputs
    val operatorObservable = BehaviorSubject.create<String>()
    val formattedStopsAndDurationObservable = BehaviorSubject.create<String>()
    val formattedTimesObservable = BehaviorSubject.create<String>()
    val legOptionObservable = BehaviorSubject.create<LegOption>()
    val selectedRailOfferObservable = BehaviorSubject.create<RailSearchResponse.RailOffer>()
    val fareDescriptionLabelObservable = BehaviorSubject.create<String>()

    init {
        railOfferObserver.subscribe {
            val legOption: LegOption = it.outboundLeg!!

            val formattedStopsAndDuration = Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                    .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                    .put("formattedchangecount", RailViewModel.formatChangesText(context, legOption.changesCount())).format().toString()
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
