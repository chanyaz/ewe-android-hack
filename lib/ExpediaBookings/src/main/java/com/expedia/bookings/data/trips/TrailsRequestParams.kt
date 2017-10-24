package com.expedia.bookings.data.trips

data class TrailsRequestParams(
        val latitude: String,
        val longitude: String,
        val key: String,
        val limit: String,
        val distance: String
)