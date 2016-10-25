package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailInboundResultsViewModel(context: Context) : BaseRailResultsViewModel(context) {
    val outboundLegOptionSubject = PublishSubject.create<RailLegOption>()
    val outboundOfferSubject = PublishSubject.create<RailOffer>()
    val resultsReturnedSubject = BehaviorSubject.create<RailSearchResponse>()

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_return))
        outboundOfferSubject.withLatestFrom(resultsReturnedSubject, { offer, response ->
            Pair(getLegOptionsForOffer(offer, response), response.inboundLeg?.cheapestPrice)
        }).subscribe(legOptionsAndCheapestPriceSubject)
    }

    private fun getLegOptionsForOffer(offer: RailOffer, response: RailSearchResponse): List<RailLegOption> {
        return response.getInboundLegOptionsForOffer(offer)
    }
}