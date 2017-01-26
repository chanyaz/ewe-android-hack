package com.expedia.bookings.data

import org.joda.time.DateTime


class HotelItinDetailsResponse : ItinDetailsResponse() {

    class HotelResponseData : ResponseData() {
        val hotels = emptyList<Hotels>()
    }

    class Hotels {
        var bookingStatus: String? = null
        var hotelId: String? = null
        var checkInDateTime: DateTime? = null
        var checkOutDateTime: DateTime? = null
    }

}
