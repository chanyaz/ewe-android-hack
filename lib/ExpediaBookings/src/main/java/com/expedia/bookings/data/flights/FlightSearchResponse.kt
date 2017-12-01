package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseApiResponse

class FlightSearchResponse : BaseApiResponse() {
    var legs: List<FlightLeg> = emptyList()
    var offers: List<FlightTripDetails.FlightOffer> = emptyList()
    lateinit var obFeesDetails: String
    var hasSubPub = false
    var cachedResultsFound: Boolean? = null
    var bookable: Boolean? = null
    var searchType = FlightSearchType.NORMAL

    fun isResponseCached(): Boolean {
        return cachedResultsFound != null
    }

    fun areCachedResultsBookable(): Boolean {
        return bookable ?: false
    }

    fun areCachedResultsNonBookable(): Boolean {
        val bookableResults = bookable
        return bookableResults != null && !bookableResults
    }

    enum class FlightSearchType {
        NORMAL,
        CACHED,
        GREEDY,
        CACHED_GREEDY
    }
}