package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailTripSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailOffer>()
    val railLegObserver = PublishSubject.create<RailLegOption>()

    //Outputs
    val formattedDatesObservable = railLegObserver.map { railLegOption ->
        Phrase.from(context, R.string.rail_checkout_outbound_TEMPLATE)
                .put("date", DateFormatUtils.formatLocalDateToShortDayAndMonth(railLegOption.departureDateTime.toDateTime()))
                .format().toString()
    }
    val fareDescriptionObservable = railOfferObserver.map { offer -> offer.railProductList.first().aggregatedFareDescription }
    val railCardNameObservable = railOfferObserver.map { offer ->
        if (offer.hasRailCardApplied()) offer.railProductList.first().fareQualifierList.first().name else ""
    }
}
