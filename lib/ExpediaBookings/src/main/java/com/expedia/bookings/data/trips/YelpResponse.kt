package com.expedia.bookings.data.trips

data class YelpAccessToken(
        val access_token: String,
        val token_type: String,
        val expires_in: Int
)

data class YelpResponse(val businesses: List<YelpBusiness>)

data class YelpBusiness(
        val is_closed: Boolean,
        val name: String,
        val url: String,
        val image_url: String,
        val coordinates: YelpBusinessLocation
)

data class YelpBusinessLocation(
        val latitude: Double,
        val longitude: Double
)