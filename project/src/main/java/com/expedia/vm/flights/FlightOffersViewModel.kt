package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import java.util.HashMap
import java.util.LinkedHashSet

class FlightOffersViewModel(context: Context, flightServices: FlightServices) : BaseFlightOffersViewModel(context, flightServices) {

    private lateinit var flightMap: HashMap<String, LinkedHashSet<FlightLeg>>

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
                    outboundLeg.packageOfferModel = makeOffer(offer, true)
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
        if (isFlightGreedySearchEnabled(context) && isGreedyCallCompleted && !isGreedyCallAborted) {
            greedyOutboundResultsObservable.onNext(outBoundFlights.toList())
            hasUserClickedSearchObservable.onNext(searchParamsObservable.value != null)
            isGreedyCallCompleted = false
        } else {
            outboundResultsObservable.onNext(outBoundFlights.toList())
        }
    }

    private fun findInboundFlights(outboundFlightId: String): List<FlightLeg> {
        val flights = flightMap[outboundFlightId]?.toList() ?: emptyList()
        flights.forEachIndexed { index, inbound ->
            inbound.legRank = index + 1
            val offer = getFlightOffer(outboundFlightId, inbound.legId)
            if (offer != null) {
                val offerModel = makeOffer(offer, false)
                inbound.packageOfferModel = offerModel
                inbound.seatClassAndBookingCodeList = offer.offersSeatClassAndBookingCode?.last()
            }
        }
        return flights
    }

    override fun makeFlightOffer(response: FlightSearchResponse) {
        createFlightMap(response)
    }

    override fun setSubPubAvailability(hasSubPub: Boolean) {
         isSubPub = hasSubPub
    }
}
