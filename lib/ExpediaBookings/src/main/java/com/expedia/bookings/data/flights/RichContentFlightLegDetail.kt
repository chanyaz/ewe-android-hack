package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightLegDetail {
    @SerializedName("leg")
    var flightLegList: List<RichContentFlightLeg> = emptyList()

    class RichContentFlightLeg {
        var id = ""
        @SerializedName("segmentList")
        var flightSegmentDetail: RichContentFlightSegmentDetail? = null
    }
}
