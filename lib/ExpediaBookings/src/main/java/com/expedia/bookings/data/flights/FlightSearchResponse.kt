package com.expedia.bookings.data.flights

import com.expedia.bookings.data.cars.BaseApiResponse

class FlightSearchResponse : BaseApiResponse() {
    var legs: List<FlightLeg> = emptyList()
    var offers: List<FlightTripDetails.FlightOffer> = emptyList()
}