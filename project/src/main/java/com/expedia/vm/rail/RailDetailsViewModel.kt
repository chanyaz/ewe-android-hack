package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.utils.RailUtils
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

    val railLegOptionSubject = BehaviorSubject.create<RailLegOption>()
    val railOffersPairSubject = BehaviorSubject.create<Pair<List<RailOffer>, Money?>>()

    val overtaken = railLegOptionSubject.map { railLegOption ->
        if (railLegOption != null) railLegOption.overtakenJourney else false
    }

    init {
        railLegOptionSubject.subscribe { railLegOption ->
            formattedTimeIntervalSubject.onNext(DateTimeUtils.formatInterval(context, railLegOption.getDepartureDateTime(),
                    railLegOption.getArrivalDateTime()))
            val changesString = RailUtils.formatRailChangesText(context, railLegOption.noOfChanges)
            formattedLegInfoSubject.onNext("${DateTimeUtils.formatDuration(context.resources,
                    railLegOption.durationMinutes())}, $changesString")
            railOffersPairSubject.onNext(Pair(railResultsObservable.value.findOffersForLegOption(railLegOption), getCompareToPrice()))
        }
    }

    //TODO for now it's hardcoded to handle just outbounds. There's another story to handle inbound delta pricing
    private fun getCompareToPrice(): Money? {
        if (railResultsObservable.value.legList.size == 2) return railResultsObservable.value.legList[1].cheapestPrice else return null
    }
}

