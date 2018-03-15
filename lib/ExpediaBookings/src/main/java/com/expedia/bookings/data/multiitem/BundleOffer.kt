package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel

data class BundleOffer(
    var hotel: Hotel?,
    var outboundFlight: FlightLeg?,
    var inboundFlight: FlightLeg?
)
