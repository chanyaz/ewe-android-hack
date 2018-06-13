package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject

object ItinMocker {
    val hotelDetailsNoPriceDetails = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_no_price_for_mocker.json")?.itin!!
    val hotelDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
    val hotelDetailsHappyMultipleRooms = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_with_multiple_rooms_for_mocker.json")?.itin!!
    val hotelDetailsPosSameAsPoSu = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_pos_same_as_posu.json")?.itin!!
    val hotelDetailsPaidWithPointsPartial = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_paid_with_partial_points.json")?.itin!!
    val hotelDetailsPaidWithPointsFull = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_paid_with_full_points.json")?.itin!!
    val hotelDetailsExpediaCollect = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_expedia_collect.json")?.itin!!
    val hotelPackageHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/itin_package_mock.json")?.itin!!
    val mickoHotelHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/itin_micko_happy.json")?.itin!!
    val mickoMultiHotel = mockObject(ItinDetailsResponse::class.java, "api/trips/itin_micko_multi_hotel.json")?.itin!!
    val packageEmpty = mockObject(ItinDetailsResponse::class.java, "api/trips/package_trip_details_no_package.json")?.itin!!
    val lxDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details.json")?.itin!!
    val lxDetailsAlsoHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_for_mocker.json")?.itin!!
    val lxDetailsNoDetailsUrl = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details_no_detail_url.json")?.itin!!
    val lxDetailsNoTripID = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details_no_trip_id.json")?.itin!!
    val lxDetailsInvalidLatLong = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details_no_trip_id.json")?.itin!!
    val lxDetailsNoLatLong = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_no_dates.json")?.itin!!
    val lxDetailsNoLat = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_without_email.json")?.itin!!
    val lxDetailsNoVendorPhone = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_without_vendor_phone_number.json")?.itin!!
    val lxDetailsNoOrderNumber = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_with_email.json")?.itin!!
    val lxDetailsNoDates = mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_no_dates.json")?.itin!!
    val carDetailsBadLocations = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_faulty_locations.json")?.itin!!
    val carDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_happy.json")?.itin!!
    val carDetailsHappyPickupDropOffSame = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_happy_drop_pickup_same_spot.json")?.itin!!
    val carDetailsBadNameAndImage = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_bad_name_image.json")?.itin!!
    val carDetailsBadPickupAndTimes = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details_bad_pickup_bad_times.json")?.itin!!
    val midMultipleFlightsTripDetails = mockObject(ItinDetailsResponse::class.java, "api/trips/mid_multiple_flights_trip_details.json")?.itin!!
    val itinMickoMultiDestinationFlight = mockObject(ItinDetailsResponse::class.java, "api/trips/itin_micko_flight_multi_destination.json")?.itin!!
    val flightDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/flight_trip_details.json")?.itin!!
    val flightDetailsHappySplitTicket = mockObject(ItinDetailsResponse::class.java, "api/trips/flight_trip_details_split_ticket.json")?.itin!!
    val flightDetailsHappyMultiSegment = mockObject(ItinDetailsResponse::class.java, "api/trips/flight_trip_details_multi_segment.json")?.itin!!
}
