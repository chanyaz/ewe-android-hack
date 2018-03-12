package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyFlightCriteria {
    @SerializedName("airTravelerCategoryList")
    var travelerDetail: RouteHappyFlightTravelerDetail? = null
}
