package com.expedia.bookings.data

data class GaiaSuggestionRequest(
    val lat: Double,
    val lng: Double,
    val sortType: String,
    val lob: String,
    val locale: String,
    val siteId: Int,
    val misForRealWorldEnabled: Boolean
)
