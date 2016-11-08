package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailTripSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailCreateTripResponse.RailTripOffer>()
    val railOutboundLegObserver = PublishSubject.create<RailLegOption>()
    val railInboundLegObserver = PublishSubject.create<RailLegOption>()

    //Outputs
    val formattedOutboundDateObservable = railOutboundLegObserver.map { railLegOption ->
        Phrase.from(context, R.string.rail_checkout_outbound_TEMPLATE)
                .put("date", DateFormatUtils.formatLocalDateToShortDayAndMonth(railLegOption.departureDateTime.toDateTime()))
                .format().toString()
    }
    val formattedInboundDateObservable = railInboundLegObserver.map { railLegOption ->
        Phrase.from(context, R.string.rail_checkout_inbound_TEMPLATE)
                .put("date", DateFormatUtils.formatLocalDateToShortDayAndMonth(railLegOption.arrivalDateTime.toDateTime()))
                .format().toString()
    }

    val moreInfoOutboundClicked = PublishSubject.create<Unit>()
    val moreInfoInboundClicked = PublishSubject.create<Unit>()
}
