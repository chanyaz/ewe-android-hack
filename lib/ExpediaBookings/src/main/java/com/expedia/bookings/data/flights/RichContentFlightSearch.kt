package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightSearch {
    @SerializedName("tripGeometry")
    var tripType = ""
    var flightCriteria: RichContentFlightCriteria? = null
}

enum class TripType(val type: String) {
    ONEWAY("ONEWAY"),
    ROUND_TRIP("ROUNDTRIP")
}
