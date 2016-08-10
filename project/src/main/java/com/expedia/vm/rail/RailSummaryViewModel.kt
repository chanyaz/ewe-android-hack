package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class RailSummaryViewModel(context: Context) {
    val railOfferObserver = BehaviorSubject.create<RailOffer>()

    //Outputs
    val formattedDatesObservable = BehaviorSubject.create<String>()
    val selectedOfferObservable = BehaviorSubject.create<RailOffer>()

    init {
        railOfferObserver.subscribe { offer ->
            val legOption: RailLegOption = offer.outboundLeg!!

            val formattedDate = Phrase.from(context, R.string.rail_checkout_outbound_TEMPLATE)
                    .put("date", DateFormatUtils.formatLocalDateToShortDayAndMonth(legOption.departureDateTime.toDateTime()))
                    .format().toString()

            formattedDatesObservable.onNext(formattedDate)
            selectedOfferObservable.onNext(offer)
        }
    }
}
