package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailInboundResultsViewModel(context: Context) : BaseRailResultsViewModel(context) {
    val outboundLegOptionSubject = PublishSubject.create<RailLegOption>()
    val outboundOfferSubject = PublishSubject.create<RailOffer>()
    val resultsReturnedSubject = BehaviorSubject.create<RailSearchResponse>()

    val openReturnSubject = PublishSubject.create<Boolean>()

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_return))
        outboundOfferSubject.withLatestFrom(resultsReturnedSubject, { offer, response ->
            openReturnSubject.onNext(offer.isOpenReturn)
            Pair(getLegOptionsForOffer(offer, response), response.inboundLeg?.cheapestPrice)
        }).subscribe(legOptionsAndCheapestPriceSubject)
    }

    private fun getLegOptionsForOffer(offer: RailOffer, response: RailSearchResponse): List<RailLegOption> {
        return response.getInboundLegOptionsForOffer(offer)
    }
}
