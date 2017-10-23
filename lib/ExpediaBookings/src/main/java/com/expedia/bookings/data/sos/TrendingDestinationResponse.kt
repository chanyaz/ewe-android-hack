package com.expedia.bookings.data.sos

class TrendingDestinationResponse {

    var trendinglocations: List<TrendingLocation>? = null
        internal set

    fun hasError(): Boolean {
        return trendinglocations == null
    }
}