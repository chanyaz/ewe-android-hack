package com.expedia.bookings.data.flights

class KrazyGlueResponse {
    var success: Boolean? = false
    lateinit var destinationName: String
    lateinit var xSellHotels: List<KrazyGlueHotel>
}
