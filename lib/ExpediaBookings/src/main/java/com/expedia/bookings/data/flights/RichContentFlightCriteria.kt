package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContentFlightCriteria {
    @SerializedName("airTravelerCategoryList")
    var travelerDetail: RichContentFlightTravelerDetail? = null
}
