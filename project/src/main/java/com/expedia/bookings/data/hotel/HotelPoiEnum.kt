package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class HotelPoiEnum(val iconId: Int, val priority: Int) {
    RESTAURANTS(R.drawable.ic_poi_restaurant, 0),
    LANDMARK(R.drawable.ic_poi_camera, 1),
    SHOPPING(R.drawable.ic_poi_shopping, 2),
    NIGHTLIFE(R.drawable.ic_poi_nightlife, 3),
    TRANSPORTSTATIONS(R.drawable.ic_poi_train, 4);

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
