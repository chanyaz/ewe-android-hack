package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.widget.RailViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailDetailsViewModel(val context: Context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()

    val offerSelectedObservable = PublishSubject.create<RailOffer>()
    val showAmenitiesObservable = PublishSubject.create<RailOffer>()
    val showFareRulesObservable = PublishSubject.create<RailOffer>()

    val formattedLegInfoSubject = BehaviorSubject.create<CharSequence>()
    val formattedTimeIntervalSubject = BehaviorSubject.create<CharSequence>()

    val railLegSubject = BehaviorSubject.create<RailLegOption>()
    val railOfferListSubject = BehaviorSubject.create<List<RailOffer>>()
    val overtaken = railLegSubject.map { railLegOption ->
        if (railLegOption != null) railLegOption.overtakenJourney else false
    }

    init {
        railLegSubject.subscribe { railLegOption ->
            formattedTimeIntervalSubject.onNext(DateTimeUtils.formatInterval(context, railLegOption.getDepartureDateTime(),
                    railLegOption.getArrivalDateTime()))
            val changesString = RailViewModel.formatChangesText(context, railLegOption.noOfChanges)
            formattedLegInfoSubject.onNext("${DateTimeUtils.formatDuration(context.resources,
                    railLegOption.durationMinutes())}, $changesString")

            railOfferListSubject.onNext(railResultsObservable.value.findOffersForLegOption(railLegOption))
        }
    }
}

