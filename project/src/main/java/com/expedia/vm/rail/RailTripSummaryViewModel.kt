package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class RailTripSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailOffer>()
    val railLegObserver = BehaviorSubject.create<RailLegOption>()

    //Outputs
    val formattedDatesObservable = BehaviorSubject.create<String>()
    val fareDescriptionObservable = BehaviorSubject.create<String>()

    init {
        railOfferObserver.subscribe { offer ->
            fareDescriptionObservable.onNext(offer.railProductList.first().aggregatedFareDescription)
        }

        railLegObserver.subscribe { railLegOption ->
            val formattedDate = Phrase.from(context, R.string.rail_checkout_outbound_TEMPLATE)
                    .put("date", DateFormatUtils.formatLocalDateToShortDayAndMonth(railLegOption.departureDateTime.toDateTime()))
                    .format().toString()

            formattedDatesObservable.onNext(formattedDate)
        }
    }
}
