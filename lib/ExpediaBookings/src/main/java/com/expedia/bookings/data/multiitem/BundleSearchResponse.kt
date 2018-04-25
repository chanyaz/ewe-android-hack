package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageOfferModel

interface BundleSearchResponse {
    fun getHotelCheckInDate(): String
    fun getHotelCheckOutDate(): String
    fun getHotelResultsCount(): Int
    fun getHotels(): List<Hotel>
    fun getFlightLegs(): List<FlightLeg>
    fun hasSponsoredHotelListing(): Boolean
    fun getCurrencyCode(): String?
    fun getFlightPIIDFromSelectedHotel(hotelKey: String?): String?
    fun getSelectedFlightPIID(outboundLegId: String?, inboundLegId: String?): String?
    fun isSplitTicketFlights(outboundLegId: String?, inboundLegId: String?): Boolean
    fun getCurrentOfferPrice(): PackageOfferModel.PackagePrice?
    fun setCurrentOfferPrice(offerPrice: PackageOfferModel.PackagePrice)
    fun hasErrors(): Boolean
    fun getRatePlanCode(): String?
    fun getRoomTypeCode(): String?
    val firstError: PackageErrorDetails.PackageAPIErrorDetails
    fun getBundleRoomResponse(): List<HotelOffersResponse.HotelRoomResponse>
    fun hasRoomResponseErrors(): Boolean
    val roomResponseFirstErrorCode: PackageErrorDetails.ApiErrorDetails
}
