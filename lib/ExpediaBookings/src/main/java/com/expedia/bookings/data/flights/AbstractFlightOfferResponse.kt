package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse

abstract class AbstractFlightOfferResponse: TripResponse() {
    lateinit open var details: FlightTripDetails

    open var totalPriceIncludingFees: Money? = null
    open var selectedCardFees: Money? = null
}
