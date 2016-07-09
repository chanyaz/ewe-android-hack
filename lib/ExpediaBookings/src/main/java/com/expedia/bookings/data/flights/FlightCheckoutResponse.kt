package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money

class FlightCheckoutResponse() : FlightCreateTripResponse() {
    private val flightDetailResponse: FlightTripDetails? = null
    val currencyCode: String? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null

    override fun getDetails(): FlightTripDetails? {
        return flightDetailResponse
    }
}
