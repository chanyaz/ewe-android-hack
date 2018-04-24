package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject

object ItinMocker {
    val hotelDetailsNoPriceDetails = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_no_price_for_mocker.json")?.itin!!
    val hotelDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
    val hotelDetailsHappyMultipleRooms = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_with_multiple_rooms_for_mocker.json")?.itin!!
    val hotelPackageHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/itin_package_mock.json")?.itin!!
    val lxDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details.json")?.itin!!
    val lxDetailsAlsoHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_for_mocker.json")?.itin!!
    val lxDetailsNoDetailsUrl = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details_no_detail_url.json")?.itin!!
    val lxDetailsNoTripID = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details_no_trip_id.json")?.itin!!
}
