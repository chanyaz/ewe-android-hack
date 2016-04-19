package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money

class PackageCheckoutResponse() : BaseApiResponse() {
    val newTrip: TripDetails? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null
}

data class TripDetails(
        val itineraryNumber: String? = null,
        val travelRecordLocator: String? = null,
        val tripId: String? = null)
