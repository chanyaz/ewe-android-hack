package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel
import java.util.ArrayList
import java.util.HashMap

data class MultiItemApiSearchResponse(
        val offers: List<MultiItemOffer>,
        val hotels: Map<String, HotelOffer>,
        val flights: Map<String, FlightOffer>,
        val flightLegs: Map<String, MultiItemFlightLeg>,
        val errors: List<MultiItemError>?
) : BundleSearchResponse, BundleHotelRoomResponse {

    private lateinit var sortedHotels: List<Hotel>
    private lateinit var sortedFlights: List<FlightLeg>
    private lateinit var hotelRooms : List<HotelOffersResponse.HotelRoomResponse>
    private var currentSelectedOffer: PackageOfferModel? = null

    fun setup(): BundleSearchResponse {
        if (hasErrors()) {
            return this
        }
        sortedHotels = ArrayList()
        sortedFlights = ArrayList()

        val convertedHotels = HashMap<String, Hotel>()
        val convertedFlightLegs = HashMap<String, FlightLeg>()

        offers.map { offer ->
            (offer.packagedOffers + offer.searchedOffer).map { (productType, productKey) ->
                when (productType) {
                    ProductType.Air -> {
                        val flight = flights[productKey] as FlightOffer

                        val outboundLegId = flight.legIds[0]
                        val outboundFlight = convertedFlightLegs[outboundLegId] ?: FlightLeg.convertMultiItemFlightLeg(outboundLegId, flight, flightLegs[outboundLegId], offer)
                        outboundFlight.outbound = true
                        if (convertedFlightLegs[outboundLegId] == null) {
                            convertedFlightLegs[outboundLegId] = outboundFlight
                        }
                        sortedFlights += outboundFlight

                        val inboundLegId = flight.legIds[1]
                        val inboundFlight = convertedFlightLegs[inboundLegId] ?: FlightLeg.convertMultiItemFlightLeg(inboundLegId, flight, flightLegs[inboundLegId], offer)
                        if (convertedFlightLegs[inboundLegId] == null) {
                            convertedFlightLegs[inboundLegId] = inboundFlight
                        }
                        sortedFlights += inboundFlight
                    }
                    ProductType.Hotel -> {
                        val hotel = convertedHotels[productKey] ?: Hotel.convertMultiItemHotel(hotels[productKey], offer)
                        convertedHotels[productKey] = hotel
                        sortedHotels += hotel
                    }
                    else -> {
                    }
                }
            }
        }
        return this
    }

    override fun getHotelCheckInDate(): String {
        return hotels.values.elementAt(0).checkInDate
    }

    override fun getHotelCheckOutDate(): String {
        return hotels.values.elementAt(0).checkOutDate
    }

    override fun getHotelResultsCount(): Int {
        return getHotels().size
    }

    override fun getHotels(): List<Hotel> {
        return sortedHotels
    }

    override fun getFlightLegs(): List<FlightLeg> {
        return sortedFlights
    }

    override fun hasSponsoredHotelListing(): Boolean {
        return false
    }

    override fun getCurrencyCode(): String {
        return offers[0].price.totalPrice.currency
    }

    override fun getCurrentOfferModel(): PackageOfferModel {
        return currentSelectedOffer!!
    }

    override fun setCurrentOfferModel(offerModel: PackageOfferModel) {
        currentSelectedOffer = offerModel
    }

    override fun hasErrors(): Boolean {
        return errors?.isNotEmpty() ?: false
    }

    override val firstError: PackageApiError.Code
        get() {
            if (errors == null || errors.isEmpty()) {
                throw RuntimeException("No errors to get!")
            }
            return when (errors.first().key) {
                //TODO to be handled
                else -> PackageApiError.Code.pkg_unknown_error
            }
        }

    // MARK :- Hotel Room Response
    override fun getBundleRoomResponse(): List<HotelOffersResponse.HotelRoomResponse> {
        hotelRooms = ArrayList()
        if (hasRoomResponseErrors()) {
            return hotelRooms
        }
        offers.map { offer ->
            hotelRooms += HotelOffersResponse.convertMidHotelRoomResponse(hotels[(offer.searchedOffer).productKey], offer)
        }
        return hotelRooms
    }

    override fun hasRoomResponseErrors(): Boolean {
        return errors?.isNotEmpty() ?: false
    }

    override val roomResponseFirstError: ApiError
        get() {
            if (errors == null || errors.isEmpty()) {
                throw RuntimeException("No errors to get!")
            }
            return when (errors.first().key) {
                //TODO to be handled
                else -> ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            }
        }

}