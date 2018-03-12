package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RouteHappyRichContent {
    var legId = ""
    var score = 0.0f
    @SerializedName("superlative")
    var scoreExpression = ""
    var legAmenities: RouteHappyAmenity? = null
    var segmentAmenitiesList: List<RouteHappyAmenity> = emptyList()

    class RouteHappyAmenity {
        var wifi = false
        var entertainment = false
        var power = false
    }
}
