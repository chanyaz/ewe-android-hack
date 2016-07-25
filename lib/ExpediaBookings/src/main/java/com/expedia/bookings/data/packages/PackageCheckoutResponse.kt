package com.expedia.bookings.data.packages

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails

class PackageCheckoutResponse() : PackageCreateTripResponse() {
    val newTrip: TripDetails? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null
}
