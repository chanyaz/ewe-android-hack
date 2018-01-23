package com.expedia.bookings.data.flights

import com.google.gson.annotations.SerializedName

class KrazyglueResponse {
    var success: Boolean = false
    var destinationDeepLink: String? = null
    @SerializedName("xsellHotels")
    lateinit var krazyglueHotels: List<KrazyglueHotel>

    class KrazyglueHotel {
        lateinit var hotelId: String
        lateinit var hotelName: String
        lateinit var starRating: String
        lateinit var guestRating: String
        lateinit var standAlonePrice: String
        lateinit var airAttachedPrice: String
        lateinit var hotelImage: String
    }

    lateinit var xsellError: XSellError

    class XSellError {
        lateinit var errorCause: String
    }
}
