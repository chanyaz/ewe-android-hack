package com.expedia.bookings.data.flights

import com.expedia.bookings.data.AbstractSupportsFeesOfferResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse

abstract class AbstractFlightOfferResponse: AbstractSupportsFeesOfferResponse() {
    lateinit open var details: FlightTripDetails
}
