package com.expedia.vm.flights

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.isRichContentEnabled
import java.util.HashMap
import java.util.LinkedHashSet

class FlightOffersViewModel(context: Context, flightServices: FlightServices) : BaseFlightOffersViewModel(context, flightServices) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var flightMap: HashMap<String, LinkedHashSet<FlightLeg>>

    override fun selectOutboundFlight(legId: String) {
        inboundResultsObservable.onNext(findInboundFlights(legId))
    }

    override fun createFlightMap(response: FlightSearchResponse) {
        val outBoundFlights: LinkedHashSet<FlightLeg> = LinkedHashSet()
        val offers = response.offers
        val legs = response.legs

        flightMap = HashMap()
        flightOfferModels = HashMap<String, FlightTripDetails.FlightOffer>()
        offers.forEach { offer ->
            val outboundId = offer.legIds.first()
            val inboundId = offer.legIds.last()
            val outboundLeg = legs.find { it.legId == outboundId }
            val inboundLeg = legs.find { it.legId == inboundId }

            flightOfferModels.put(makeFlightOfferKey(outboundId, inboundId), offer)
            if (outboundLeg != null) {
                // assuming all offers are sorted by price by API
                val hasCheapestOffer = !outBoundFlights.contains(outboundLeg)
                if (hasCheapestOffer) {
                    outboundLeg.packageOfferModel = makeOffer(offer)
                    outboundLeg.seatClassAndBookingCodeList = offer.offersSeatClassAndBookingCode?.first()
                }
                outBoundFlights.add(outboundLeg)
                if (outboundLeg.legRank == 0) {
                    outboundLeg.legRank = outBoundFlights.size
                }
            }
            var flights = flightMap[outboundId]
            if (flights == null) {
                flights = LinkedHashSet()
            }
            if (inboundLeg != null) {
                flights.add(inboundLeg)
            }
            flightMap.put(outboundId, flights)
        }
        sendOutboundFlights(outBoundFlights)
    }

    private fun findInboundFlights(outboundFlightId: String): List<FlightLeg> {
        val flights = flightMap[outboundFlightId]?.toList() ?: emptyList()
        flights.forEachIndexed { index, inbound ->
            if (isRichContentEnabled(context)) {
                inbound.richContent = null
            }
            inbound.legRank = index + 1
            val offer = getFlightOffer(outboundFlightId, inbound.legId)
            if (offer != null) {
                val offerModel = makeOffer(offer)
                inbound.packageOfferModel = offerModel
                inbound.seatClassAndBookingCodeList = offer.offersSeatClassAndBookingCode?.last()
            }
        }
        return flights
    }

    override fun makeFlightOffer(response: FlightSearchResponse) {
        createFlightMap(response)
    }
}
