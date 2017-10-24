package com.expedia.bookings.utils

import org.joda.time.LocalDate


class DeeplinkCreatorUtils() {

    companion object {
        var hotelSearchParams: HotelSearchParams? = null
        var hotelSelectionParams: HotelSelectionParams? = null
        var hotelRoomSelectionParams: HotelRoomSelectionParams? = null
    }


}

class HotelRoomSelectionParams {
    lateinit var selectedRoomTypeCode: String
}

class HotelSearchParams {
    lateinit var origin: String
    lateinit var destination: String
    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate
    lateinit var originID: String
    lateinit var destinationID: String
}

class HotelSelectionParams {
    lateinit var selectedHotelID: String
}
