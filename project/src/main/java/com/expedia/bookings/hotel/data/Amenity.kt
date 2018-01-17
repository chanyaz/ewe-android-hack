package com.expedia.bookings.hotel.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import java.util.ArrayList
import java.util.Comparator
import java.util.TreeSet

enum class Amenity(@DrawableRes val drawableRes: Int,
                   @StringRes val filterDescriptionId: Int?,
                   @StringRes val propertyDescriptionId: Int,
                   val priority: Int) {

    POOL(R.drawable.ic_amenity_pool, R.string.amenity_pool, R.string.amenity_pool, priority = 0),
    POOL_INDOOR(R.drawable.ic_amenity_pool, null, R.string.amenity_pool_indoor, priority = 0),
    POOL_OUTDOOR(R.drawable.ic_amenity_pool, null, R.string.amenity_pool_outdoor, priority = 0),
    INTERNET(R.drawable.ic_amenity_internet, R.string.filter_high_speed_internet, R.string.amenity_internet, priority = 1),
    BREAKFAST(R.drawable.ic_amenity_breakfast, R.string.filter_free_breakfast, R.string.amenity_breakfast, priority = 2),
    PARKING(R.drawable.ic_amenity_local_parking, R.string.amenity_free_parking, R.string.amenity_parking, priority = 3),
    EXTENDED_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.amenity_parking, priority = 3),
    FREE_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.amenity_parking, priority = 3),
    PETS(R.drawable.ic_amenity_pets, R.string.filter_pets_allowed, R.string.amenity_pets_allowed, priority = 4),
    RESTAURANT(R.drawable.ic_amenity_restaurant, null, R.string.amenity_restaurant, priority = 5),
    FITNESS_CENTER(R.drawable.ic_amenity_fitness_center, null, R.string.amenity_fitness_center, priority = 6),
    ROOM_SERVICE(R.drawable.ic_amenity_room_service, null, R.string.amenity_room_service, priority = 7),
    SPA(R.drawable.ic_amenity_spa, null, R.string.amenity_spa, priority = 8),
    BUSINESS_CENTER(R.drawable.ic_amenity_business_center, null, R.string.amenity_business_center, priority = 9),
    AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, R.string.filter_free_airport_transportation, R.string.amenity_free_airport_shuttle, priority = 10),
    HOT_TUB(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_hot_tub, priority = 11),
    JACUZZI(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_jacuzzi, priority = 11),
    WHIRLPOOL_BATH(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_whirlpool_bath, priority = 11),
    KITCHEN(R.drawable.ic_amenity_kitchen, null, R.string.amenity_kitchen, priority = 12),
    KIDS_ACTIVITIES(R.drawable.ic_amenity_kid_activities, null, R.string.amenity_kids_activities, priority = 13),
    BABYSITTING(R.drawable.ic_amenity_babysitting, null, R.string.amenity_babysitting, priority = 14),
    ALL_INCLUSIVE(R.drawable.ic_amenity_all_inclusive, R.string.filter_all_inclusive, R.string.amenity_all_inclusive, priority = 15),
    AC_UNIT(R.drawable.ic_amenity_ac_unit, R.string.amenity_air_conditioning, R.string.amenity_air_conditioning, priority = 16),
    ACCESSIBLE_BATHROOM(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_bathroom, priority = 17),
    ROLL_IN_SHOWER(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_bathroom, priority = 17),
    IN_ROOM_ACCESSIBILITY(R.drawable.ic_amenity_accessible, null, R.string.amenity_in_room_accessibility, priority = 17),
    ACCESSIBLE_PATHS(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_paths, priority = 18),
    HANDICAPPED_PARKING(R.drawable.ic_amenity_accessible, null, R.string.amenity_handicapped_parking,  priority = 18),
    DEAF_ACCESSIBILITY_EQUIPMENT(R.drawable.ic_amenity_accessible, null, R.string.amenity_deaf_accessibility_equipment,  priority = 18),
    BRAILLE_SIGNAGE(R.drawable.ic_amenity_accessible, null, R.string.amenity_braille_signage,  priority = 18);

    companion object {
        fun getFilterAmenities() : List<Amenity> {
            return listOf(Amenity.BREAKFAST, Amenity.POOL, Amenity.PARKING,
                    Amenity.PETS, Amenity.INTERNET, Amenity.AIRPORT_SHUTTLE,
                    Amenity.AC_UNIT, Amenity.ALL_INCLUSIVE)
        }

        fun getSearchKey(amenity: Amenity) : String {
            return when (amenity) {
                Amenity.BREAKFAST -> "16"
                Amenity.POOL -> "7"
                Amenity.PARKING -> "14"
                Amenity.INTERNET -> "19"
                Amenity.PETS -> "17"
                Amenity.AIRPORT_SHUTTLE -> "66"
                Amenity.AC_UNIT -> "27"
                Amenity.ALL_INCLUSIVE -> "30"
                else -> ""
            }
        }

        fun amenitiesToShow(list: List<HotelOffersResponse.HotelAmenities>): List<Amenity> {
            val amenityTreeSet = TreeSet<Amenity>(AmenityComparator())

            for (i in 0 until list.size) {
                val amenity = getPropertyAmenity(list[i].id)
                amenity?.let { amenityTreeSet.add(it) }
            }

            if (amenityTreeSet.isEmpty()) return emptyList()

            return ArrayList<Amenity>(amenityTreeSet)
        }

        private fun getPropertyAmenity(key: String) : Amenity? {
            when (key) {
                "2065", "2213", "2538" -> return Amenity.BUSINESS_CENTER
                "9" -> return Amenity.FITNESS_CENTER
                "371" -> return Amenity.HOT_TUB
                "2046", "2097", "2100", "2101", "2125", "2126", "2127", "2156", "2191", "2192",
                "2220", "2390", "2392", "2394", "2403", "2405", "2407" -> return Amenity.INTERNET
                "2186" -> return Amenity.KIDS_ACTIVITIES
                "312", "2158", "2208" -> return Amenity.KITCHEN
                "51", "2338" -> return Amenity.PETS
                "14", "24", "2074", "2138", "2859" -> return Amenity.POOL
                "19" -> return Amenity.RESTAURANT
                "2017", "2123", "2341" -> return Amenity.SPA
                "361", "2001", "2077", "2098", "2102", "2103", "2104", "2105", "2193", "2194",
                "2205", "2209", "2210", "2211" -> return Amenity.BREAKFAST
                "6" -> return Amenity.BABYSITTING
                "28", "2011", "2013", "2109", "2110", "2132", "2133", "2195", "2215", "2216",
                "2553", "2798" -> return Amenity.PARKING
                "20", "2015", "2053" -> return Amenity.ROOM_SERVICE
                "2419" -> return Amenity.ACCESSIBLE_PATHS
                "2420" -> return Amenity.ACCESSIBLE_BATHROOM
                "2421" -> return Amenity.ROLL_IN_SHOWER
                "2422" -> return Amenity.HANDICAPPED_PARKING
                "2423" -> return Amenity.IN_ROOM_ACCESSIBILITY
                "2424" -> return Amenity.DEAF_ACCESSIBILITY_EQUIPMENT
                "2425" -> return Amenity.BRAILLE_SIGNAGE
                "10", "2196", "2214", "2353" -> return Amenity.AIRPORT_SHUTTLE
                else -> return null
            }
        }

        private class AmenityComparator : Comparator<Amenity> {
            override fun compare(lhs: Amenity, rhs: Amenity): Int {
                return lhs.priority.minus(rhs.priority)
            }
        }
    }
}
