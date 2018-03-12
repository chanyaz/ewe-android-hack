package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyRichInfoDetail {
    @SerializedName("richInfo")
    var richInfoList: List<RouteHappyRichInfo> = emptyList()

    class RouteHappyRichInfo {
        val id = "1"
        @SerializedName("searchContext")
        var flightSearch: RouteHappyFlightSearch? = null
        @SerializedName("odlist")
        var flightOfferDetail: RouteHappyFlightOfferDetail? = null
    }
}
