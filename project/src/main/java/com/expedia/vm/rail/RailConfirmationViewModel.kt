package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.StrUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.Locale

class RailConfirmationViewModel(val context: Context) {
    val confirmationObservable = PublishSubject.create<Pair<RailCheckoutResponse, String>>()
    val railOfferObserver = PublishSubject.create<RailSearchResponse.RailOffer>()

    // Outputs
    val itinNumberObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val inboundCardVisibility = BehaviorSubject.create<Boolean>()
    val outboundCardTitleObservable = BehaviorSubject.create<String>()
    val outboundCardSubTitleObservable = BehaviorSubject.create<String>()
    val inboundCardTitleObservable = BehaviorSubject.create<String>()
    val inboundCardSubTitleObservable = BehaviorSubject.create<String>()

    init {
        confirmationObservable.subscribe { pair ->
            val itinNumber = pair.first.newTrip.itineraryNumber
            val email = pair.second
            val itinNumberText = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberObservable.onNext(itinNumberText)
        }

        railOfferObserver.subscribe { offer ->
            val outbound: RailLegOption = offer.outboundLeg!!

            if (outbound != null) {
                destinationObservable.onNext(outbound.arrivalStation.stationDisplayName)
                outboundCardTitleObservable.onNext(getCardTitle(outbound))
                outboundCardSubTitleObservable.onNext(getCardSubtitle(outbound, offer.passengerList.size))
            }

            val inbound: RailLegOption? = offer.inboundLeg
            if (inbound != null) {
                inboundCardVisibility.onNext(true)
                inboundCardTitleObservable.onNext(getCardTitle(inbound))
                inboundCardSubTitleObservable.onNext(getCardSubtitle(inbound, offer.passengerList.size))
            } else {
                inboundCardVisibility.onNext(false)
            }
        }
    }

    private fun getCardSubtitle(legOption: RailLegOption, numOfTravelers: Int): String {
        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        val departureTime = JodaUtils.format(legOption.getDepartureDateTime(), dateFormat).toLowerCase(Locale.getDefault())

        val travelers = StrUtils.formatTravelerString(context, numOfTravelers)
        return Phrase.from(context, R.string.rail_departure_time_travelers_TEMPLATE)
                .put("departuretime", departureTime)
                .put("travelers", travelers)
                .format().toString()
    }

    private fun getCardTitle(legOption: RailLegOption): String {
        return Phrase.from(context, R.string.rail_departure_arrival_station_TEMPLATE)
                .put("departurestation", legOption.departureStation.stationDisplayName)
                .put("arrivalstation", legOption.arrivalStation.stationDisplayName)
                .format().toString()
    }
}