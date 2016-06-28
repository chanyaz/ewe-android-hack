package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money

class FlightCheckoutResponse() : FlightCreateTripResponse() {
    val currencyCode: String? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null
}
