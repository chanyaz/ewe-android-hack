package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightSegmentDetail {
    @SerializedName("segment")
    var flightSegmentList: List<RichContentFlightSegment> = emptyList()

    class RichContentFlightSegment {
        var id = ""
        var carrierCode = ""
        var flightNumber = ""
        var bookingCode = ""
        var flightCriteria: RichContentFlightSegmentCriteria? = null
    }
}
