package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class ItinDetailsResponse(
        val responseType: String?,
        @SerializedName("responseData") val itin: Itin?
)
