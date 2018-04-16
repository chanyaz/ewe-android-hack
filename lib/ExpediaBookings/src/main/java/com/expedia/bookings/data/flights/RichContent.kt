package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class RichContent {
    var legId = ""
    var score = 0.0f
    @SerializedName("superlative")
    var scoreExpression = ""
    var legAmenities: RichContentAmenity? = null
    var segmentAmenitiesList: List<RichContentAmenity> = emptyList()

    class RichContentAmenity {
        var wifi = false
        var entertainment = false
        var power = false
    }
}
