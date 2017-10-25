package com.expedia.bookings.data.trips

data class YelpRequestParams(
        val accessToken: String,
        val term: String,
        val latitude: String,
        val longitude: String,
        val limit: Int,
        val radius: Int,
        val sortBy: String
)