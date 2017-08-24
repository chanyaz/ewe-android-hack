package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseApiResponse

class FlightSearchResponse : BaseApiResponse() {
    var legs: List<FlightLeg> = emptyList()
    var offers: List<FlightTripDetails.FlightOffer> = emptyList()
    lateinit var obFeesDetails: String
    var hasSubPub = false
    var cachedResultsFound: Boolean? = null
    var bookable: Boolean? = null

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
}