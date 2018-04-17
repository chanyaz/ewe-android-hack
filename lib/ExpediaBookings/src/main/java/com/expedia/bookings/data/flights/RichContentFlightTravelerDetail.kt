package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightTravelerDetail(
        @SerializedName("airTravelerCategory")
        var travelerCategoryList: List<RichContentFlightTravelerCategory> = emptyList()
)
