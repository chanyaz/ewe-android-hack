package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class ItinFlight(
        val uniqueID: String?,
        val flightType: FlightType?
) : ItinLOB

enum class FlightType {
    @SerializedName("ROUND_TRIP")
    ROUND_TRIP,
    @SerializedName("ONE_WAY")
    ONE_WAY,
    @SerializedName("MULTI_DESTINATION")
    MULTI_DESTINATION
}
