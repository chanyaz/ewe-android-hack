package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.data.HotelAmenity
import java.util.ArrayList
import java.util.Comparator
import java.util.TreeSet

class HotelAmenityHelper {
    companion object {
        fun getFilterAmenities() : List<HotelAmenity> {
            return listOf(HotelAmenity.BREAKFAST, HotelAmenity.POOL, HotelAmenity.PARKING,
                    HotelAmenity.PETS, HotelAmenity.INTERNET, HotelAmenity.AIRPORT_SHUTTLE,
                    HotelAmenity.AC_UNIT, HotelAmenity.ALL_INCLUSIVE)
        }

        fun getSearchKey(amenity: HotelAmenity) : String {
            return when (amenity) {
                HotelAmenity.BREAKFAST -> "16"
                HotelAmenity.POOL -> "7"
                HotelAmenity.PARKING -> "14"
                HotelAmenity.INTERNET -> "19"
                HotelAmenity.PETS -> "17"
                HotelAmenity.AIRPORT_SHUTTLE -> "66"
                HotelAmenity.AC_UNIT -> "27"
                HotelAmenity.ALL_INCLUSIVE -> "30"
                else -> ""
            }
        }

        fun amenitiesToShow(list: List<HotelOffersResponse.HotelAmenities>): List<HotelAmenity> {
            val amenityTreeSet = TreeSet<HotelAmenity>(AmenityComparator())

            for (i in 0 until list.size) {
                val amenity = getPropertyAmenity(list[i].id)
                amenity?.let { amenityTreeSet.add(it) }
            }

            if (amenityTreeSet.isEmpty()) return emptyList()

            return ArrayList<HotelAmenity>(amenityTreeSet)
        }

        private fun getPropertyAmenity(key: String) : HotelAmenity? {
            when (key) {
                "2065", "2213", "2538" -> return HotelAmenity.BUSINESS_CENTER
                "9" -> return HotelAmenity.FITNESS_CENTER
                "371" -> return HotelAmenity.HOT_TUB
                "2046", "2097", "2100", "2101", "2125", "2126", "2127", "2156", "2191", "2192",
                "2220", "2390", "2392", "2394", "2403", "2405", "2407" -> return HotelAmenity.INTERNET
                "2186" -> return HotelAmenity.KIDS_ACTIVITIES
                "312", "2158", "2208" -> return HotelAmenity.KITCHEN
                "51", "2338" -> return HotelAmenity.PETS
                "14", "24", "2074", "2138", "2859" -> return HotelAmenity.POOL
                "19" -> return HotelAmenity.RESTAURANT
                "2017", "2123", "2341" -> return HotelAmenity.SPA
                "361", "2001", "2077", "2098", "2102", "2103", "2104", "2105", "2193", "2194",
                "2205", "2209", "2210", "2211" -> return HotelAmenity.BREAKFAST
                "6" -> return HotelAmenity.BABYSITTING
                "28", "2011", "2013", "2109", "2110", "2132", "2133", "2195", "2215", "2216",
                "2553", "2798" -> return HotelAmenity.PARKING
                "20", "2015", "2053" -> return HotelAmenity.ROOM_SERVICE
                "2419" -> return HotelAmenity.ACCESSIBLE_PATHS
                "2420" -> return HotelAmenity.ACCESSIBLE_BATHROOM
                "2421" -> return HotelAmenity.ROLL_IN_SHOWER
                "2422" -> return HotelAmenity.HANDICAPPED_PARKING
                "2423" -> return HotelAmenity.IN_ROOM_ACCESSIBILITY
                "2424" -> return HotelAmenity.DEAF_ACCESSIBILITY_EQUIPMENT
                "2425" -> return HotelAmenity.BRAILLE_SIGNAGE
                "10", "2196", "2214", "2353" -> return HotelAmenity.AIRPORT_SHUTTLE
                else -> return null
            }
        }

        class AmenityComparator : Comparator<HotelAmenity> {
            override fun compare(lhs: HotelAmenity, rhs: HotelAmenity): Int {
                return lhs.priority.minus(rhs.priority)
            }
        }
    }
}
