package com.expedia.bookings.data.multiitem

data class MultiItemFlightSegment(
        val departureAirportCode: String,
        val arrivalAirportCode: String,
        val departureCity: String,
        val arrivalCity: String,
        val departureDateTime: String,
        val arrivalDateTime: String,
        val flightNumber: String,
        val airlineCode: String,
        val bookingCode: String,
        val airlineName: String,
        val operatingAirlineCode: String,
        val operatedByAirlineName: String,
        val duration: Duration,
        val layoverDuration: Duration?,
        val distance: Distance,
        val cabinClass: String,
        val airplaneType: PlaneType?,
        val airlineLogoUrl: String,
        val elapsedDays: Int
)
