package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel
import org.jetbrains.annotations.Nullable

interface BundleSearchResponse {
    fun getHotelCheckInDate(): String
    fun getHotelCheckOutDate(): String
    fun getHotelResultsCount(): Int
    fun getHotels(): List<Hotel>
    fun getFlightLegs(): List<FlightLeg>
    fun hasSponsoredHotelListing(): Boolean
    fun getCurrencyCode(): String
    @Nullable fun getCurrentOfferModel(): PackageOfferModel
    fun setCurrentOfferModel(offerModel: PackageOfferModel)
    fun hasErrors(): Boolean
    val firstError: PackageApiError.Code
}