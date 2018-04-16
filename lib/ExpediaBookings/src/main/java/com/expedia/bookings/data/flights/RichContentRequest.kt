package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentRequest {
    @SerializedName("messageInfo")
    var requestInfo: RichContentRequestInfo? = null
    @SerializedName("richInfoList")
    var richInfoDetail: RichContentDetail? = null
}
