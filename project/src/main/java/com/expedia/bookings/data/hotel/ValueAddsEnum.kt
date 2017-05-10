package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class ValueAddsEnum(val priority: Int, val iconId: Int) {
    INTERNET(0, R.drawable.ic_amenity_wifi),
    BREAKFAST(1, R.drawable.ic_amenity_breakfast),
    PARKING(2, R.drawable.ic_amenity_parking),
    FREE_AIRPORT_SHUTTLE(3, R.drawable.ic_amenity_airport_shuttle);
}
