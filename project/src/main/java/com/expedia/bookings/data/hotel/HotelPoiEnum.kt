package com.expedia.bookings.data.hotel

import com.expedia.bookings.R

enum class HotelPoiEnum(val iconId: Int, val priority: Int) {
    RESTAURANT(R.drawable.ic_poi_restaurant, 0),
    LANDMARK(R.drawable.ic_poi_camera, 1),
    SHOPPING(R.drawable.ic_poi_shopping, 2),
    NIGHTLIFE(R.drawable.ic_poi_nightlife, 3),
    TRANSIT(R.drawable.ic_poi_train, 4);

    companion object {
        fun fromString(string: String): HotelPoiEnum? {
            return when (string) {
                "RESTAURANT" -> return RESTAURANT
                "LANDMARK" -> LANDMARK
                "NIGHTLIFE" -> NIGHTLIFE
                "TRANSIT" -> TRANSIT
                "SHOPPING" -> SHOPPING
                else -> null
            }
        }
    }
}
