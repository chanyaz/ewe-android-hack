package com.expedia.bookings.itin.tripstore.data

data class ItinFlight(
        val uniqueID: String?,
        val flightType: FlightType?
) : ItinLOB

enum class FlightType {
    ROUND_TRIP,
    ONE_WAY,
    MULTI_DESTINATION
}
