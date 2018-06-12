package com.expedia.bookings.itin.tripstore.data

data class ItinFlight(
        val uniqueID: String?,
        val flightType: FlightType?,
        val legs: List<ItinLeg>?
) : ItinLOB

enum class FlightType {
    ROUND_TRIP,
    ONE_WAY,
    MULTI_DESTINATION
}

data class ItinLeg(
        val segments: List<Flight>
)

data class Flight(
        val departureLocation: FlightLocation?,
        val arrivalLocation: FlightLocation?,
        val airlineCode: String?,
        val flightNumber: String?,
        val departureTime: ItinTime?,
        val arrivalTime: ItinTime?
)

data class FlightLocation(
        val airportCode: String?
)
