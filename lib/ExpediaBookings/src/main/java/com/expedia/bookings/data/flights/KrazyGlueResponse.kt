package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class KrazyGlueResponse {
    var success: Boolean = false
    lateinit var destinationName: String
    @SerializedName("xsellHotels")
    var krazyGlueHotels: List<KrazyGlueHotel> ?= null

    class KrazyGlueHotel {
        lateinit var hotelId: String
        lateinit var hotelName: String
        lateinit var starRating: String
        lateinit var guestRating: String
        lateinit var standAlonePrice: String
        lateinit var airAttachedPrice: String
        lateinit var hotelImage: String
    }
}
