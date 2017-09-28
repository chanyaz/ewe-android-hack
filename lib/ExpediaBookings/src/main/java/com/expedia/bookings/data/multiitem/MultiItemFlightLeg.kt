package com.expedia.bookings.data.multiitem

data class MultiItemFlightLeg(
        val segments: List<MultiItemFlightSegment>,
        val baggageFeesUrl: String,
        val stops: Int
)