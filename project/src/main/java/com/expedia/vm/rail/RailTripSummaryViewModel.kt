package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailTripOffer
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailTripSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailTripOffer>()
    val railOutboundLegObserver = PublishSubject.create<RailLegOption>()
    val railInboundLegObserver = PublishSubject.create<RailLegOption>()

    //Outputs
    val formattedOutboundDateObservable = railOutboundLegObserver.map { railLegOption ->
        Phrase.from(context, R.string.rail_checkout_outbound_TEMPLATE)
                .put("date", LocaleBasedDateFormatUtils.dateTimeToEEEMMMdd(railLegOption.departureDateTime.toDateTime()))
                .format().toString()
    }
    val formattedInboundDateObservable = railInboundLegObserver.map { railLegOption ->
        Phrase.from(context, R.string.rail_checkout_inbound_TEMPLATE)
                .put("date", LocaleBasedDateFormatUtils.dateTimeToEEEMMMdd(railLegOption.arrivalDateTime.toDateTime()))
                .format().toString()
    }

    val moreInfoOutboundClicked = PublishSubject.create<Unit>()
    val moreInfoInboundClicked = PublishSubject.create<Unit>()
}
