package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject

object ItinMocker {
    val hotelDetailsNoPriceDetails = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")?.itin!!
    val hotelDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
}
