package com.expedia.bookings.data.flights.extensions

import com.expedia.bookings.data.flights.FlightSearchParams

fun FlightSearchParams.TripType.isRoundTrip(): Boolean {
    return this == FlightSearchParams.TripType.RETURN
}

fun FlightSearchParams.TripType.isOneWay(): Boolean {
    return this == FlightSearchParams.TripType.ONE_WAY
}

fun FlightSearchParams.TripType.isMultiDest(): Boolean {
    return this == FlightSearchParams.TripType.MULTI_DEST
}
