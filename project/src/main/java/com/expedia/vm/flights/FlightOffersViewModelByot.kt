package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightSearchResponse.FlightSearchType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.flights.utils.FlightServicesManager
import java.util.HashMap
import java.util.LinkedHashSet

class FlightOffersViewModelByot(context: Context, flightServicesManager: FlightServicesManager) : BaseFlightOffersViewModel(context, flightServicesManager) {

    override fun selectOutboundFlight(legId: String) {
        val maxStay = context.resources.getInteger(R.integer.calendar_max_days_flight_search)
        val maxRange = context.resources.getInteger(R.integer.calendar_max_days_flight_search)
        val searchParams = Db.getFlightSearchParams().buildParamsForInboundSearch(maxStay, maxRange, legId)
        searchingForFlightDateTime.onNext(Unit)
        isOutboundSearch = false
        flightSearchSubscription = flightServicesManager.doFlightSearch(searchParams, FlightSearchResponse.FlightSearchType.NORMAL, successResponseHandler, errorResponseHandler)
    }

    override fun createFlightMap(type: FlightSearchType, response: FlightSearchResponse) {
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
                    outboundLeg.packageOfferModel = makeOffer(offer)
                    outboundLeg.seatClassAndBookingCodeList = offer.offersSeatClassAndBookingCode?.first()
                }
                outBoundFlights.add(outboundLeg)
                outboundLeg.legRank = outBoundFlights.size
            }
        }
        sendOutboundFlights(type, outBoundFlights)
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
                inboundLeg.packageOfferModel = makeOffer(offer)
                inboundLeg.seatClassAndBookingCodeList = offer.offersSeatClassAndBookingCode?.last()
                inboundFlights.add(inboundLeg)
                inboundLeg.legRank = inboundFlights.size
            }
        }
        inboundResultsObservable.onNext(inboundFlights.toList())
    }

    override fun makeFlightOffer(type: FlightSearchType, response: FlightSearchResponse) {
        if (isOutboundSearch) {
            createFlightMap(type, response)
        } else {
            findInboundFlights(response)
        }
    }
}
