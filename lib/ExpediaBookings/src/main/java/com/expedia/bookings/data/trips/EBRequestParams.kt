package com.expedia.bookings.data.trips

data class EBRequestParams (
    val latitude: Double,
    val longitude: Double,
    val within: String,
    val start: String,
    val end: String,
    val expand: String
    )
