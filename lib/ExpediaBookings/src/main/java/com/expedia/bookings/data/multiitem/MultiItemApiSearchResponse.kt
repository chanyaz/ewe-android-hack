package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
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
) : BundleSearchResponse {

    private lateinit var sortedHotels: List<Hotel>
    private lateinit var sortedFlights: List<FlightLeg>
    private lateinit var hotelRooms: List<HotelOffersResponse.HotelRoomResponse>
    private var currentSelectedOfferPrice: PackageOfferModel.PackagePrice? = null

    fun setup(): BundleSearchResponse {
        if (hasErrors()) {
            return this
        }
        sortedHotels = ArrayList()
        sortedFlights = ArrayList()

        val convertedHotels = HashMap<String, Hotel>()
        val convertedFlightLegs = HashMap<String, FlightLeg>()

        offers.mapIndexed { index, offer ->
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
                        offer.price?.let {
                            val hotel = convertedHotels[productKey] ?: Hotel.convertMultiItemHotel(hotels[productKey], offer, index)
                            convertedHotels[productKey] = hotel
                            sortedHotels += hotel
                        }
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

    override fun getRoomTypeCode(): String? {
        return hotels.values.elementAt(0).roomTypeCode
    }

    override fun getRatePlanCode(): String? {
        return hotels.values.elementAt(0).ratePlanCode
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

    override fun getCurrencyCode(): String? {
        return offers[0].price?.totalPrice?.currency
    }

    override fun getCurrentOfferPrice(): PackageOfferModel.PackagePrice? {
        return currentSelectedOfferPrice
    }

    override fun setCurrentOfferPrice(offerPrice: PackageOfferModel.PackagePrice) {
        currentSelectedOfferPrice = offerPrice
    }

    override fun hasErrors(): Boolean {
        return errors?.isNotEmpty() ?: false
    }

    private val midCouldNotFindResultsErrors: List<String>
        get() {
            return arrayListOf(
                    "MIS_FLIGHT_PRODUCT_NOT_FOUND",
                    "FLIGHT_DETAIL_CALL_FAILED",
                    "FLIGHT_DETAIL_CALL_FLIGHT_COMBINATION_UNAVAILABLE",
                    "FLIGHT_DETAIL_CALL_PRICING_ERROR",
                    "FLIGHT_DETAIL_CALL_ERROR_READING_FROM_CACHE",
                    "FLIGHT_DETAIL_CALL_FAILED_DUE_TO_BAD_REQUEST",
                    "MIS_HOTEL_PRODUCT_NOT_FOUND",
                    "AVAIL_SUMMARY_DATES_INVALID",
                    "AVAIL_SUMMARY_TRIP_DURATION_INVALID",
                    "AVAIL_SUMMARY_INVALID_ROOM_OCCUPANTS",
                    "AVAIL_SUMMARY_INVALID_HOTEL_ID",
                    "FSS_SELECTED_HOTEL_OFFER_NOT_FOUND",
                    "MIS_INVALID_REQUEST",
                    "MIS_FAILED_TO_MATCH_OFFERS"
            )
        }

    override val firstError: PackageErrorDetails.PackageAPIErrorDetails
        get() {
            if (errors == null || errors.isEmpty()) {
                throw RuntimeException("No errors to get!")
            }
            val errorKey = errors.first().key
            var errorCode: PackageApiError.Code
            if (midCouldNotFindResultsErrors.contains(errorKey)) {
                errorCode = PackageApiError.Code.mid_could_not_find_results
            } else if (errorKey == "MIS_ORIGIN_RESOLUTION_ERROR" ||
                    errorKey == "MIS_AMBIGUOUS_ORIGIN_ERROR") {
                errorCode = PackageApiError.Code.pkg_origin_resolution_failed
            } else if (errorKey == "MIS_DESTINATION_RESOLUTION_ERROR" ||
                    errorKey == "MIS_PROHIBITED_DESTINATION_ERROR" ||
                    errorKey == "MIS_AMBIGUOUS_DESTINATION_ERROR") {
                errorCode = PackageApiError.Code.pkg_destination_resolution_failed
            } else if (errorKey == "FSS_HOTEL_UNAVAILABLE_FOR_RED_EYE_FLIGHT") {
                errorCode = PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight
            } else if (errorKey == "MIS_POST_PROCESS_ALL_OFFERS_FILTERED") {
                errorCode = PackageApiError.Code.mid_no_offers_post_filtering
            } else {
                errorCode = PackageApiError.Code.mid_internal_server_error
            }
            return PackageErrorDetails.PackageAPIErrorDetails(errorKey, errorCode)
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

    override val roomResponseFirstErrorCode: PackageErrorDetails.ApiErrorDetails
        get() {
            if (errors == null || errors.isEmpty()) {
                throw RuntimeException("No errors to get!")
            }

            val errorKey = errors.first().key
            var errorCode: ApiError.Code
            if (midCouldNotFindResultsErrors.contains(errorKey)) {
                errorCode = ApiError.Code.PACKAGE_SEARCH_ERROR
            } else {
                errorCode = ApiError.Code.UNKNOWN_ERROR
            }
            return PackageErrorDetails.ApiErrorDetails(errorKey, errorCode)
        }

    override fun getSelectedFlightPIID(outboundLegId: String?, inboundLegId: String?): String? {
        if (outboundLegId == null || inboundLegId == null) {
            return null
        }
        return flights.values.firstOrNull { flightOffer ->
            flightOffer.legIds[0] == outboundLegId && flightOffer.legIds[1] == inboundLegId
        }?.piid
    }

    override fun isSplitTicketFlights(outboundLegId: String?, inboundLegId: String?): Boolean {
        if (outboundLegId == null || inboundLegId == null) {
            return false
        }
        return flights.values.firstOrNull { flightOffer ->
            flightOffer.legIds[0] == outboundLegId && flightOffer.legIds[1] == inboundLegId
        }?.splitTicket ?: false
    }

    override fun getFlightPIIDFromSelectedHotel(hotelKey: String?): String? {
        if (hotelKey == null) {
            return null
        }
        val offer = offers.firstOrNull { offer ->
            offer.searchedOffer.productKey == hotelKey
        }
        val flightOfferReference = offer?.packagedOffers?.firstOrNull {
            it.productType == ProductType.Air
        }
        if (flightOfferReference != null) {
            return flights[flightOfferReference.productKey]?.piid
        }
        return null
    }

    override fun getSelectedFlightReferenceTotalPriceFromPIID(piid: String?): Money? {
        val flight = flights.values.firstOrNull { offer ->
            offer.piid == piid
        }
        return flight?.let { it ->
            return it.flightOfferReferenceTotalPrice()
        }
    }

    override fun getSelectedHotelReferenceTotalPriceFromID(hotelId: String?): Money? {
        val hotel = hotels.values.firstOrNull { offer ->
            offer.id == hotelId
        }
        return hotel?.let { it ->
            return it.hotelOfferReferenceTotalPrice()
        }
    }
}
