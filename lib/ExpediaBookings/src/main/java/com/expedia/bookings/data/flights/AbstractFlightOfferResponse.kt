package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse

abstract class AbstractFlightOfferResponse: TripResponse() {
    open var totalPriceIncludingFees: Money? = null // returned from card fee service (Fees driven by payment type)
    open var selectedCardFees: Money? = null // returned from card fee service (Fees driven by payment type)

    /**
     * Helper function for details as the API uses 2 different keys for flight details
     *
     * 	FlightCreateTripResponse: details
     * 	FlightCheckoutResponse: flightDetailResponse
     *
     * @return flight details
     */
    abstract fun getDetails(): FlightTripDetails
}
