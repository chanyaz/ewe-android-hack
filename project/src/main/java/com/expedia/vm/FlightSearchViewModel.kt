package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.FlightServices
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import java.util.HashMap
import java.util.LinkedHashSet

class FlightSearchViewModel(context: Context, val flightServices: FlightServices) : DatedSearchViewModel(context) {

    override val paramsBuilder = FlightSearchParams.Builder(getMaxStay())

    var flightMap: HashMap<String, LinkedHashSet<FlightLeg>> = HashMap()
    var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer> = HashMap()

    // Outputs
    val searchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val outboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val flightProductId = PublishSubject.create<String>()
    val outboundFlightSelected = PublishSubject.create<FlightLeg>()
    val inboundFlightSelected = PublishSubject.create<FlightLeg>()

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (paramsBuilder.isDepartureSameAsOrigin()) {
                errorDepartureSameAsOrigin.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!paramsBuilder.hasValidDates()) {
                errorMaxDatesObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxStay()))
            } else {
                val flightSearchParams = paramsBuilder.build()
                searchParamsObservable.onNext(flightSearchParams)
            }
        } else {
            if (!paramsBuilder.hasDepartureAndArrival()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    init {
        searchParamsObservable.subscribe { params ->
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

    override fun getMaxStay(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_flight_search)
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

    private fun createFlightMap(response: FlightSearchResponse) {
        val outBoundFlights: LinkedHashSet<FlightLeg> = LinkedHashSet()
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
                flights  = LinkedHashSet()
            }
            if (inboundLeg != null) {
                flights.add(inboundLeg)
            }
            flightMap.put(outboundId, flights)
        }
        outboundResultsObservable.onNext(outBoundFlights.toList())
    }

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