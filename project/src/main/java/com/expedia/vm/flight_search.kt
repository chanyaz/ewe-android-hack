package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.FlightServices
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import java.util.HashMap
import java.util.HashSet

class FlightSearchViewModel(val context: Context, val flightServices: FlightServices) {
    var flightMap: HashMap<String, HashSet<FlightLeg>> = HashMap()
    var outBoundFlights: HashSet<FlightLeg> = HashSet()
    var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer> = HashMap()

    val flightParamsObservable = PublishSubject.create<FlightSearchParams>()
    val outboundFlightSelected = PublishSubject.create<FlightLeg>()
    val inboundFlightSelected = PublishSubject.create<FlightLeg>()

    // Outputs
    val outboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val flightProductId = PublishSubject.create<String>()

    init {
        flightParamsObservable.subscribe { params ->
            flightServices.flightSearch(params).subscribe(makeResultsObserver())
        }

        outboundFlightSelected.subscribe { flight ->
            inboundResultsObservable.onNext(findInboundFlights(flight.legId))
        }

        Observable.combineLatest(outboundFlightSelected, inboundFlightSelected, { outbound, inbound ->
            val offer = flightOfferModels[outbound.legId+inbound.legId]
            flightProductId.onNext(offer?.productKey)
        }).subscribe()
    }

    fun makeResultsObserver(): Observer<FlightSearchResponse> {
        return object : Observer<FlightSearchResponse> {
            override fun onNext(response: FlightSearchResponse) {
                createFlightMap(response)
            }

            override fun onCompleted() {
                println("flight completed")
            }

            override fun onError(e: Throwable?) {
                println("flight error: " + e?.message)
            }
        }
    }

    //TODO: Test this
    private fun createFlightMap(response: FlightSearchResponse) {
        val offers = response.offers
        val legs = response.legs
        offers.forEach { offer ->
            val outboundId = offer.legIds.first()
            val inboundId = offer.legIds.last()
            flightOfferModels.put(outboundId + inboundId, offer)

            val outboundLeg = legs.find { it.legId == outboundId }
            outboundLeg?.packageOfferModel = makeOffer(offer)

            val inboundLeg = legs.find { it.legId == inboundId }
            if (outboundLeg != null) {
                outBoundFlights.add(outboundLeg)
            }
            var flights = flightMap[outboundId]
            if (flights == null) {
                flights  = HashSet()
            }
            if (inboundLeg != null) {
                flights.add(inboundLeg)
            }
            flightMap.put(outboundId, flights)
        }
        outboundResultsObservable.onNext(outBoundFlights.toList())
    }

    //TODO: Test this
    private fun makeOffer(offer : FlightTripDetails.FlightOffer) : PackageOfferModel {
        val offerModel = PackageOfferModel()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = offer.seatsRemaining
        val price = PackageOfferModel.PackagePrice()
        price.differentialPriceFormatted = offer.totalFarePrice.formattedPrice
        price.packageTotalPriceFormatted = offer.totalFarePrice.formattedPrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        return offerModel
    }

    //TODO: Test this
    fun findInboundFlights(flightId : String) : List<FlightLeg> {
        val flights = flightMap[flightId]?.toList() ?: emptyList()
        flights.forEach { inbound ->
            val offer = flightOfferModels[flightId+inbound.legId]
            if (offer != null) {
                val offerModel = makeOffer(offer)
                inbound.packageOfferModel = offerModel
            }
        }
        return flights
    }
}