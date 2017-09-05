package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.rail.util.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.Collections

open class RailDetailsViewModel(val context: Context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()

    val offerSelectedObservable = PublishSubject.create<RailOffer>()
    val showAmenitiesObservable = PublishSubject.create<RailOffer>()
    val showFareRulesObservable = PublishSubject.create<RailOffer>()

    val formattedLegInfoSubject = BehaviorSubject.create<CharSequence>()
    val formattedTimeIntervalSubject = BehaviorSubject.create<CharSequence>()

    val railLegOptionSubject = BehaviorSubject.create<RailLegOption>()
    val railOffersAndInboundCheapestPricePairSubject = BehaviorSubject.create<Pair<List<RailOffer>, Money?>>()

    val overtaken = railLegOptionSubject.map { railLegOption ->
        railLegOption?.overtakenJourney ?: false
    }

    init {
        railLegOptionSubject.subscribe { railLegOption ->
            formattedTimeIntervalSubject.onNext(RailUtils.formatTimeIntervalToDeviceFormat(context, railLegOption.getDepartureDateTime(),
                    railLegOption.getArrivalDateTime()))
            val changesString = RailUtils.formatRailChangesText(context, railLegOption.noOfChanges)
            formattedLegInfoSubject.onNext("${DateTimeUtils.formatDuration(context.resources,
                    railLegOption.durationMinutes())}, $changesString")

            // filter offers for one-way and round trip
            val filteredOffers = filterFareOptions(railResultsObservable.value.findOffersForLegOption(railLegOption))
            Collections.sort(filteredOffers)
            railOffersAndInboundCheapestPricePairSubject.onNext(Pair(filteredOffers, getInboundLegCheapestPrice()))
        }
    }

    open fun filterFareOptions (railOffers: List<RailOffer>): List<RailOffer> {
        // for open return display one instance of fare class
        val railSearchResponse = railResultsObservable.value
        val filteredOfferList = railSearchResponse.filterOutboundOffers(railOffers)
        return filteredOfferList
    }

    private fun getInboundLegCheapestPrice(): Money? {
        val railSearchResponse = railResultsObservable.value

        if (railSearchResponse.hasInbound()) {
            return railSearchResponse.inboundLeg?.cheapestPrice
        } else return null
    }
}

