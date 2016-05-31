package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
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
    val formattedDatesObservable = BehaviorSubject.create<String>()
    val legOptionObservable = BehaviorSubject.create<RailSearchResponse.LegOption>()

    init {
        railOfferObserver.subscribe {
            if (it.outboundLeg != null) {
                val legOption: RailSearchResponse.LegOption = it.outboundLeg!!

                val formattedStopsAndDuration = Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                        .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                        .put("formattedchangecount", RailViewModel.formatChangesText(context, legOption.changesCount())).format().toString()
                formattedStopsAndDurationObservable.onNext(formattedStopsAndDuration)

                val formattedTimes = DateTimeUtils.formatInterval(context, legOption.getDepartureDateTime(), legOption.getArrivalDateTime())
                formattedTimesObservable.onNext(formattedTimes.toString())
                val formattedDate = DateFormatUtils.formatDateToShortDayAndDate(context, legOption.departureDateTime.toDateTime())
                formattedDatesObservable.onNext(formattedDate)
                operatorObservable.onNext(legOption.allOperators())
                legOptionObservable.onNext(legOption)
            }
        }
    }
}