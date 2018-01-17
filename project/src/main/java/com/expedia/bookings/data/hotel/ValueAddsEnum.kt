package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class ValueAddsEnum(val iconId: Int, val descriptionId: Int) {
    INTERNET(R.drawable.ic_amenity_internet, R.string.amenity_free_internet),
    BREAKFAST(R.drawable.ic_amenity_breakfast, R.string.amenity_breakfast),
    PARKING(R.drawable.ic_amenity_local_parking, R.string.amenity_parking),
    FREE_AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, R.string.amenity_free_airport_shuttle);
}
