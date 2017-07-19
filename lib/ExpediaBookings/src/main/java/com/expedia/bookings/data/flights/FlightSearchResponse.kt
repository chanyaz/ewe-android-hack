package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseApiResponse

class FlightSearchResponse : BaseApiResponse() {
    var legs: List<FlightLeg> = emptyList()
    var offers: List<FlightTripDetails.FlightOffer> = emptyList()
    lateinit var obFeesDetails: String
    var hasSubPub = false
}