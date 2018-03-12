package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightLegDetail {
    @SerializedName("leg")
    var flightLegList: List<RouteHappyFlightLeg> = emptyList()

    class RouteHappyFlightLeg {
        var id = ""
        @SerializedName("segmentList")
        var flightSegmentDetail: RouteHappyFlightSegmentDetail? = null
    }
}
