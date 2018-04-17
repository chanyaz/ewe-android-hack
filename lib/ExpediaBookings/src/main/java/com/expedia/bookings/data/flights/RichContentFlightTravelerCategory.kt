package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightTravelerCategory {
    @SerializedName("airTravelerCategoryCode")
    var travelerCode = ""
    @SerializedName("airTravelerCategoryCount")
    var travelerCount = 0
}

enum class TravelerCode(val code: String) {
    ADULT("adult"),
    CHILD("child")
}
