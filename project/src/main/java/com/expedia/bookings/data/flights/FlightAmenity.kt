package com.expedia.bookings.data.flights

import com.expedia.bookings.R

enum class FlightBagAmenity(val key: Int) {
    ONE_LUGGAGE(R.string.amenity_key_one_luggage),
    TWO_LUGGAGE(R.string.amenity_key_two_luggage),
    THREE_LUGGAGE(R.string.amenity_key_three_luggage),
    FOUR_LUGGAGE(R.string.amenity_key_four_luggage),
    BAGS(R.string.amenity_key_bags),
    CARRY_ON_BAG(R.string.amenity_key_carry_on_bag)
}

enum class FlightCancellationAmenity(val key: Int) {
    REFUND_BEFORE_DEPARTURE(R.string.amenity_key_refund_before_departure),
    REFUND_AFTER_DEPARTURE(R.string.amenity_key_refund_after_departure)
}

enum class FlightSeatReservationAmenity(val key: Int) {
    SEAT_RESERVATION(R.string.amenity_key_seat_reservation)
}

enum class FlightAmenityCategory(val key: Int, val dispStr: Int) {
    INCLUDED(R.string.amenity_key_family_included, R.string.fare_family_included),
    CHARGEABLE(R.string.amenity_key_family_chargeable, R.string.fare_family_fee_applies),
    NOTOFFERED(R.string.amenity_key_family_not_offered, R.string.fare_family_not_available),
    UNKNOWN(R.string.amenity_key_family_unknown, R.string.fare_family_unknown)
}

data class AmenityResourceType(val resourceId: Int, val dispVal: String)