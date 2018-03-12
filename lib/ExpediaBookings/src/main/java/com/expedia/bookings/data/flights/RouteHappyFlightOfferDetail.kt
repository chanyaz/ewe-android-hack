package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightOfferDetail {
    @SerializedName("od")
    var flightOfferList: List<RouteHappyFlightOffer> = emptyList()

    class RouteHappyFlightOffer {
        @SerializedName("id")
        var naturalKey = ""
        @SerializedName("legList")
        var flightLegDetail: RouteHappyFlightLegDetail? = null
    }
}
