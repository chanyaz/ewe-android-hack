package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightSearch {
    @SerializedName("tripGeometry")
    var tripType = ""
    var flightCriteria: RouteHappyFlightCriteria? = null
}

enum class TripType(val type: String) {
    ONEWAY("ONEWAY"),
    ROUND_TRIP("ROUNDTRIP")
}
