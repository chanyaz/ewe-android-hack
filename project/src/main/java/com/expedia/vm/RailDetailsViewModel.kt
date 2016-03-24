package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.widget.RailViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject

class RailDetailsViewModel(val context: Context) {
    val offerViewModel = RailOfferViewModel(context)
}

class RailOfferViewModel(val context: Context) {
    val formattedTimeIntervalSubject = BehaviorSubject.create<CharSequence>()
    val offerSubject = BehaviorSubject.create<RailSearchResponse.RailOffer>()
    val formattedLegInfoSubject = BehaviorSubject.create<CharSequence>()

    init {
        offerSubject.subscribe {
            if (it.outboundLeg != null) {
                val leg = it.outboundLeg!!
                formattedTimeIntervalSubject.onNext(DateTimeUtils.formatInterval(context, leg.getDepartureDateTime(), leg.getArrivalDateTime()))

                val changesString = RailViewModel.formatChangesText(context, leg.changesCount())
                formattedLegInfoSubject.onNext("${DateTimeUtils.formatDuration(context.resources, leg.durationInMinutes)}, $changesString")
            }
        }
    }
}
