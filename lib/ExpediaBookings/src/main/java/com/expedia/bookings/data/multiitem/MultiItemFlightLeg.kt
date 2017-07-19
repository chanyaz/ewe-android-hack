package com.expedia.bookings.data.multiitem

data class MultiItemFlightLeg(
    val legId: String,
    val segments: List<MultiItemFlightSegment>,
    val stops: Int
)