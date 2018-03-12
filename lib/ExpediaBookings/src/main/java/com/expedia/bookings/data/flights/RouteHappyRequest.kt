package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyRequest {
    @SerializedName("messageInfo")
    var requestInfo: RouteHappyRequestInfo? = null
    @SerializedName("richInfoList")
    var richInfoDetail: RouteHappyRichInfoDetail? = null
}
