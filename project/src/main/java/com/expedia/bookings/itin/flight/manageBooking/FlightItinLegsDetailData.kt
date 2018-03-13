package com.expedia.bookings.itin.flight.manageBooking

data class FlightItinLegsDetailData(
        val imagePath: String?,
        val departureAirportCode: String,
        val arrivalAirportCode: String,
        val departureMonthDay: String,
        val departureTime: String,
        val arrivalMonthDay: String,
        val arrivalTime: String,
        val stopNumber: String?
)
