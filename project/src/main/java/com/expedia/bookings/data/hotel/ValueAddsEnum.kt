package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class ValueAddsEnum(val iconId: Int, val descriptionId: Int) {
    INTERNET(R.drawable.ic_amenity_internet, R.string.AmenityFreeInternet),
    BREAKFAST(R.drawable.ic_amenity_breakfast, R.string.AmenityBreakfast),
    PARKING(R.drawable.ic_amenity_local_parking, R.string.AmenityParking),
    FREE_AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, R.string.AmenityFreeAirportShuttle);
}
