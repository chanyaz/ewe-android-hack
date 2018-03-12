package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightSegmentDetail {
    @SerializedName("segment")
    var flightSegmentList: List<RouteHappyFlightSegment> = emptyList()

    class RouteHappyFlightSegment {
        var id = ""
        var carrierCode = ""
        var flightNumber = ""
        var bookingCode = ""
        var flightCriteria: RouteHappyFlightSegmentCriteria? = null
    }
}
