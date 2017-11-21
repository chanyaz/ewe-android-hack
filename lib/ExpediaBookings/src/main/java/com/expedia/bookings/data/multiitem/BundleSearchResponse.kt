package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel

interface BundleSearchResponse {
    fun getHotelCheckInDate(): String
    fun getHotelCheckOutDate(): String
    fun getHotelResultsCount(): Int
    fun getHotels(): List<Hotel>
    fun getFlightLegs(): List<FlightLeg>
    fun hasSponsoredHotelListing(): Boolean
    fun getCurrencyCode(): String
    fun getFlightPIIDFromSelectedHotel(hotelKey: String?): String?
    fun getSelectedFlightPIID(outboundLegId: String?, inboundLegId: String?): String?
    fun getCurrentOfferPrice(): PackageOfferModel.PackagePrice?
    fun setCurrentOfferPrice(offerPrice: PackageOfferModel.PackagePrice)
    fun hasErrors(): Boolean
    val firstError: PackageApiError.Code
}