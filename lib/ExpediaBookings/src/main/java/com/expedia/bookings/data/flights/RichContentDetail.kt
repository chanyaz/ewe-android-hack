package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentDetail {
    @SerializedName("richInfo")
    var richInfoList: List<RichContentInfo> = emptyList()

    class RichContentInfo {
        val id = "1"
        @SerializedName("searchContext")
        var flightSearch: RichContentFlightSearch? = null
        @SerializedName("odlist")
        var flightOfferDetail: RichContentFlightOfferDetail? = null
    }
}
