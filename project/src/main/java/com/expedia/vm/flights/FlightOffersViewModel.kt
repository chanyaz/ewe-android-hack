package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashMap
import java.util.LinkedHashSet

class FlightOffersViewModel( context: Context,
                             flightSearchResponseSubject: PublishSubject<FlightSearchResponse>,
                             val isRoundTripSearchSubject: BehaviorSubject<Boolean> ) {

    val confirmedOutboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val confirmedInboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val outboundSelected = BehaviorSubject.create<FlightLeg>()
    val inboundSelected = PublishSubject.create<FlightLeg>()
    val offerSelectedChargesObFeesSubject = BehaviorSubject.create<String>()
    val flightOfferSelected = PublishSubject.create<FlightTripDetails.FlightOffer>()
    val flightProductId = PublishSubject.create<String>()
    val showChargesObFeesSubject = PublishSubject.create<Boolean>()
    val outboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val obFeeDetailsUrlObservable = BehaviorSubject.create<String>()

    private var isRoundTripSearch = true
    private lateinit var flightMap: HashMap<String, LinkedHashSet<FlightLeg>>
    private lateinit var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer>

    init {
        flightSearchResponseSubject.subscribe {
            val obFeeDetailsUrl =
                    if (getAirlineChargesPaymentFees()) {
                        PointOfSale.getPointOfSale().airlineFeeBasedOnPaymentMethodTermsAndConditionsURL
                    } else {
                        it.obFeesDetails
                    }
            obFeeDetailsUrlObservable.onNext(obFeeDetailsUrl)
            createFlightMap(it)
        }

        isRoundTripSearchSubject.subscribe {
            isRoundTripSearch = it
        }

        setupFlightSelectionObservables()

        outboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        inboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        showChargesObFeesSubject.subscribe { hasObFee ->
            if (hasObFee || doAirlinesChargeAdditionalFees()) {
                val paymentFeeText = context.resources.getString(if (doAirlinesChargeAdditionalFees()) R.string.airline_fee_notice_payment else R.string.payment_and_baggage_fees_may_apply)
                offerSelectedChargesObFeesSubject.onNext(paymentFeeText)
            }
        }
    }

    private fun getAirlineChargesPaymentFees(): Boolean {
        return PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()
    }

    private fun selectFlightOffer(outboundLegId: String, inboundLegId: String) {
        val offer = getFlightOffer(outboundLegId, inboundLegId)
        if (offer != null) {
            flightProductId.onNext(offer.productKey)
            flightOfferSelected.onNext(offer)
        }
    }

    private fun setupFlightSelectionObservables() {
        confirmedOutboundFlightSelection.subscribe { flight ->
            if (isRoundTripSearch) {
                selectOutboundFlight(flight.legId)
            }
            else { // one-way flights
                val outboundLegId = flight.legId
                val inboundLegId = flight.legId // yes, they are the same. It will get us the flight offer
                selectFlightOffer(outboundLegId, inboundLegId)
            }
        }

        // return trip flights
        confirmedInboundFlightSelection.subscribe {
            val inboundLegId = it.legId
            val outboundLegId = confirmedOutboundFlightSelection.value.legId
            selectFlightOffer(outboundLegId, inboundLegId)
        }

        // fires offer selected before flight selection confirmed to lookup terms, fees etc. in offer
        inboundSelected.subscribe {
            val offer = getFlightOffer(outboundSelected.value.legId, it.legId)
            flightOfferSelected.onNext(offer)
        }
    }

    private fun doAirlinesChargeAdditionalFees(): Boolean {
        return PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()
    }

    private fun getFlightOffer(outboundLegId: String, inboundLegId: String): FlightTripDetails.FlightOffer? {
        return flightOfferModels[makeFlightOfferKey(outboundLegId, inboundLegId)]
    }

    private fun makeFlightOfferKey(outboundId: String, inboundId: String): String {
        return outboundId + inboundId
    }

    private fun selectOutboundFlight(legId: String) {
        inboundResultsObservable.onNext(findInboundFlights(legId))
    }

    private fun createFlightMap(response: FlightSearchResponse) {
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
                }
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

    private fun findInboundFlights(outboundFlightId: String) : List<FlightLeg> {
        val flights = flightMap[outboundFlightId]?.toList() ?: emptyList()
        flights.forEach { inbound ->
            val offer = getFlightOffer(outboundFlightId, inbound.legId)
            if (offer != null) {
                val offerModel = makeOffer(offer)
                inbound.packageOfferModel = offerModel
            }
        }
        return flights
    }

    private fun makeOffer(offer : FlightTripDetails.FlightOffer) : PackageOfferModel {
        val offerModel = PackageOfferModel()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = offer.seatsRemaining
        val price = PackageOfferModel.PackagePrice()
        price.packageTotalPrice = offer.totalFarePrice
        price.differentialPriceFormatted = offer.totalFarePrice.formattedPrice
        price.packageTotalPriceFormatted = offer.totalFarePrice.formattedPrice
        price.averageTotalPricePerTicket = offer.averageTotalPricePerTicket
        price.pricePerPersonFormatted = offer.averageTotalPricePerTicket.formattedWholePrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        return offerModel
    }
}
