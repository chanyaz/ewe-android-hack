package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class ValueAddsEnum(val iconId: Int, val jsonKey: String) {
    INTERNET(R.drawable.ic_amenity_internet, "internet"),
    BREAKFAST(R.drawable.ic_amenity_breakfast, "breakfast"),
    PARKING(R.drawable.ic_amenity_local_parking, "parking"),
    FREE_AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, "airportShuttle");
}
