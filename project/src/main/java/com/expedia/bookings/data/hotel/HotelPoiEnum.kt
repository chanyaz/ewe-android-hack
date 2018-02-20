package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class HotelPoiEnum(val iconId: Int, val priority: Int) {
    RESTAURANTS(R.drawable.ic_amenity_internet, 0),
    LANDMARK(R.drawable.ic_amenity_airport_shuttle, 1),
    SHOPPING(R.drawable.ic_amenity_hot_tub, 2),
    NIGHTLIFE(R.drawable.ic_amenity_breakfast, 3),
    TRANSPORTSTATIONS(R.drawable.ic_amenity_local_parking, 4);

    companion object {
        fun fromString(string: String): HotelPoiEnum? {
            return when (string) {
                "RESTAURANTS" -> return RESTAURANTS
                "LANDMARK" -> LANDMARK
                "NIGHTLIFE" -> NIGHTLIFE
                "TRANSPORTSTATIONS" -> TRANSPORTSTATIONS
                "SHOPPING" -> SHOPPING
                else -> null
            }
        }
    }
}
