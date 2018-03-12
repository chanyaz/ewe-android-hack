package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightTravelerDetail(
        @SerializedName("airTravelerCategory")
        var travelerCategoryList: List<RouteHappyFlightTravelerCategory> = emptyList()
)
