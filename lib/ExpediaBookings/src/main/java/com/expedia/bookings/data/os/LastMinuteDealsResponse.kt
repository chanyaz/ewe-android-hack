package com.expedia.bookings.data.os

import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.data.BaseDealsResponse
import com.google.gson.annotations.SerializedName

open class LastMinuteDealsResponse : BaseDealsResponse() {
    var offers: Offers = Offers()
        protected set

    inner class Offers {
        @SerializedName("Hotel")
        var hotels: List<DealsDestination.Hotel> = emptyList()
    }
}
