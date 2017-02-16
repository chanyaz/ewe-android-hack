package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import java.util.HashMap
import java.util.LinkedHashSet

class FlightOffersViewModelV2(context: Context, flightServices: FlightServices) : AbstractFlightOfferViewModel(context, flightServices) {

    override fun selectOutboundFlight(legId: String) {
        val maxStay = context.resources.getInteger(R.integer.calendar_max_days_flight_search)
        val maxRange = context.resources.getInteger(R.integer.calendar_max_days_flight_search)
        val searchParams = Db.getFlightSearchParams().buildParamsForInboundSearch(maxStay, maxRange, legId)
        flightInboundSearchSubscription = flightServices.flightSearch(searchParams, makeResultsObserver(), resultsReceivedDateTimeObservable)
    }

    override fun createFlightMap(response: FlightSearchResponse) {
        val outBoundFlights: LinkedHashSet<FlightLeg> = LinkedHashSet()
        val offers = response.offers
        val legs = response.legs
        if (!isRoundTripSearch) {
            flightOfferModels = HashMap<String, FlightTripDetails.FlightOffer>()
        }
        offers.forEach { offer ->
            val outboundId = offer.legIds.first()
            val outboundLeg = legs.find { it.legId == outboundId }
            if (!isRoundTripSearch) {
                flightOfferModels.put(makeFlightOfferKey(outboundId, outboundId), offer)
            }
            if (outboundLeg != null) {
                // assuming all offers are sorted by price by API
                val hasCheapestOffer = !outBoundFlights.contains(outboundLeg)
                if (hasCheapestOffer) {
                    outboundLeg.packageOfferModel = makeOffer(offer, true)
                }
                outBoundFlights.add(outboundLeg)
            }
        }
        outboundResultsObservable.onNext(outBoundFlights.toList())
    }

    private fun findInboundFlights(response: FlightSearchResponse) {
        val inboundFlights: LinkedHashSet<FlightLeg> = LinkedHashSet()
        val outboundId = outboundSelected.value.legId
        val offers = response.offers
        val legs = response.legs
        flightOfferModels = HashMap<String, FlightTripDetails.FlightOffer>()
        offers.forEach { offer ->
            val inboundId = offer.legIds.last()
            val inboundLeg = legs.find { it.legId == inboundId }
            flightOfferModels.put(makeFlightOfferKey(outboundId, inboundId), offer)
            if (inboundLeg != null) {
                inboundLeg.packageOfferModel = makeOffer(offer, false)
                inboundFlights.add(inboundLeg)
            }
        }
        inboundResultsObservable.onNext(inboundFlights.toList())
    }

    override fun makeFlightOffer(response: FlightSearchResponse) {
        if (isOutboundSearch) {
            createFlightMap(response)
            isOutboundSearch = false
        } else {
            findInboundFlights(response)
        }
    }
}
