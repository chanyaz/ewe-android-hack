package com.expedia.bookings.data.packages

import com.expedia.bookings.data.cars.BaseApiResponse

class PackageCheckoutResponse() : BaseApiResponse() {
    val newTrip: TripDetails? = null
}

data class TripDetails(
        val itineraryNumber: String? = null,
        val travelRecordLocator: String? = null,
        val tripId: String? = null)
