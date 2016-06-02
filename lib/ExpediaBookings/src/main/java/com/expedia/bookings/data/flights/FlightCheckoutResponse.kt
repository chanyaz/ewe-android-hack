package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails

class FlightCheckoutResponse() : FlightCreateTripResponse() {
    val orderId: String? = null
    val totalChargesPrice: Money? = null
}
