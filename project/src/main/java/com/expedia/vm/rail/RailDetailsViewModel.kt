package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.utils.rail.RailUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class RailDetailsViewModel(val context: Context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()

    val offerSelectedObservable = PublishSubject.create<RailOffer>()
    val showAmenitiesObservable = PublishSubject.create<RailOffer>()
    val showFareRulesObservable = PublishSubject.create<RailOffer>()

    val formattedLegInfoSubject = BehaviorSubject.create<CharSequence>()
    val formattedTimeIntervalSubject = BehaviorSubject.create<CharSequence>()

    val railLegOptionSubject = BehaviorSubject.create<RailLegOption>()
    val railOffersAndInboundCheapestPricePairSubject = BehaviorSubject.create<Pair<List<RailOffer>, Money?>>()

    val overtaken = railLegOptionSubject.map { railLegOption ->
        if (railLegOption != null) railLegOption.overtakenJourney else false
    }

    init {
        railLegOptionSubject.subscribe { railLegOption ->
            formattedTimeIntervalSubject.onNext(RailUtils.formatTimeInterval(context, railLegOption.getDepartureDateTime(),
                    railLegOption.getArrivalDateTime()))
            val changesString = RailUtils.formatRailChangesText(context, railLegOption.noOfChanges)
            formattedLegInfoSubject.onNext("${DateTimeUtils.formatDuration(context.resources,
                    railLegOption.durationMinutes())}, $changesString")

            // for open return display one instance of fare class
            val filteredOffers = filterOpenReturnFareOptions(railResultsObservable.value.findOffersForLegOption(railLegOption))
            railOffersAndInboundCheapestPricePairSubject.onNext(Pair(filteredOffers, getInboundLegCheapestPrice()))
        }
    }

    private fun getInboundLegCheapestPrice(): Money? {
        val railSearchResponse = railResultsObservable.value

        if (railSearchResponse.hasInbound()) {
            return railSearchResponse.inboundLeg?.cheapestPrice
        } else return null
    }

    private fun filterOpenReturnFareOptions(railOffers: List<RailOffer>): List<RailOffer> {
        val fareServiceKeys = ArrayList<String>()
        val filteredOfferList = railOffers.orEmpty().filter { shouldAddOffer(it, fareServiceKeys) }
        return filteredOfferList
    }

    private fun shouldAddOffer(railOffer: RailOffer, fareServiceKeys: ArrayList<String>): Boolean {
        //add the offer if it is not open return
        if (!railOffer.isOpenReturn) return true

        // creating a key by combining fare class, service class and total fare amount
        val currentKey = railOffer.uniqueIdentifier
        if (!fareServiceKeys.contains(currentKey)) {
            fareServiceKeys.add(currentKey)
            return true
        }
        return false
    }
}

