package com.expedia.bookings.data.multiitem

data class FlightLeg(
    val legId: String,
    val segments: List<FlightSegment>,
    val stops: Int
)