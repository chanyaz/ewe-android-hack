package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightOfferDetail {
    @SerializedName("od")
    var flightOfferList: List<RichContentFlightOffer> = emptyList()

    class RichContentFlightOffer {
        @SerializedName("id")
        var naturalKey = ""
        @SerializedName("legList")
        var flightLegDetail: RichContentFlightLegDetail? = null
    }
}
