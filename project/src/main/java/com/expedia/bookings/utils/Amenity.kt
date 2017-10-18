package com.expedia.bookings.utils

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import java.util.ArrayList
import java.util.Comparator
import java.util.TreeSet

/**
 * Created by mohsharma on 8/12/15.
 */

// preference number tells order we want to show in hotel details. if we change the preference number it will change.
enum class Amenity(val preference: Int, val resId: Int, val strId: Int) {
    POOL(0, R.drawable.ic_amenity_pool, R.string.AmenityPool),
    POOL_INDOOR(0, R.drawable.ic_amenity_pool, R.string.AmenityPoolIndoor),
    POOL_OUTDOOR(0, R.drawable.ic_amenity_pool, R.string.AmenityPoolOutdoor),
    INTERNET(1, R.drawable.ic_amenity_internet, R.string.AmenityInternet),
    BREAKFAST(2, R.drawable.ic_amenity_breakfast, R.string.AmenityBreakfast),
    PARKING(3, R.drawable.ic_amenity_parking, R.string.AmenityParking),
    EXTENDED_PARKING(3, R.drawable.ic_amenity_parking, R.string.AmenityParking),
    FREE_PARKING(3, R.drawable.ic_amenity_parking, R.string.AmenityParking),
    PETS_ALLOWED(4, R.drawable.ic_amenity_pets, R.string.AmenityPetsAllowed),
    RESTAURANT(5, R.drawable.ic_amenity_restaurant, R.string.AmenityRestaurant),
    FITNESS_CENTER(6, R.drawable.ic_amenity_fitness_center, R.string.AmenityFitnessCenter),
    ROOM_SERVICE(7, R.drawable.ic_amenity_room_service, R.string.AmenityRoomService),
    SPA(8, R.drawable.ic_amenity_spa, R.string.AmenitySpa),
//    BUSINESS_CENTER(9, R.drawable.ic_amenity_business, R.string.AmenityBusinessCenter),
    FREE_AIRPORT_SHUTTLE(10, R.drawable.ic_amenity_airport_shuttle, R.string.AmenityFreeAirportShuttle),
    ACCESSIBLE_BATHROOM(11, R.drawable.ic_amenity_accessible_bathroom, R.string.AmenityAccessibleBathroom),
    HOT_TUB(12, R.drawable.ic_amenity_hot_tub, R.string.AmenityHotTub),
    JACUZZI(13, R.drawable.ic_amenity_jacuzzi, R.string.AmenityJacuzzi),
    WHIRLPOOL_BATH(14, R.drawable.ic_amenity_whirl_pool, R.string.AmenityWhirlpoolBath),
    KITCHEN(15, R.drawable.ic_amenity_kitchen, R.string.AmenityKitchen),
    KIDS_ACTIVITIES(16, R.drawable.ic_amenity_children_activities, R.string.AmenityKidsActivities),
    BABYSITTING(17, R.drawable.ic_amenity_baby_sitting, R.string.AmenityBabysitting),
    ACCESSIBLE_PATHS(18, R.drawable.ic_amenity_accessible_ramp, R.string.AmenityAccessiblePaths),
    ROLL_IN_SHOWER(19, R.drawable.ic_amenity_accessible_shower, R.string.AmenityAccessibleBathroom),
    HANDICAPPED_PARKING(20, R.drawable.ic_amenity_handicap_parking, R.string.AmenityHandicappedParking),
    IN_ROOM_ACCESSIBILITY(21, R.drawable.ic_amenity_accessible_room, R.string.AmenityInRoomAccessibility),
    DEAF_ACCESSIBILITY_EQUIPMENT(22, R.drawable.ic_amenity_deaf_access, R.string.AmenityDeafAccessibilityEquipment),
    BRAILLE_SIGNAGE(23, R.drawable.ic_amenity_braille_signs, R.string.AmenityBrailleSignage);


    // static helper method to add amenities
    companion object {

        fun addHotelAmenity(viewGroup: ViewGroup, amenityList: List<Amenity>) {
            viewGroup.removeAllViews()
            val srcColor = ContextCompat.getColor(viewGroup.context, R.color.hotelsv2_amenity_icon_color)
            val mode = PorterDuff.Mode.SRC_ATOP
            val filter = PorterDuffColorFilter(srcColor, mode)
            val paint = Paint()
            paint.colorFilter = filter

            for (index in 0..amenityList.size - 1) {

                val amenityLayout = com.mobiata.android.util.Ui.inflate<LinearLayout>(R.layout.new_amenity_row, viewGroup, false)
                val amenityTextView = amenityLayout.findViewById<android.widget.TextView>(R.id.amenity_label)
                val amenityIconView = amenityLayout.findViewById<ImageView>(R.id.amenity_icon)
                amenityIconView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
                val amenityStr = viewGroup.context.getString(amenityList[index].strId)

                amenityTextView.text = amenityStr
                amenityIconView.setImageDrawable(ContextCompat.getDrawable(viewGroup.context, amenityList[index].resId))
                viewGroup.addView(amenityLayout)
            }
        }

        fun amenitiesToShow(list: List<HotelOffersResponse.HotelAmenities>): List<Amenity> {

            val amenityTreeSet = TreeSet<Amenity>(AmenityComparator())

            for (i in 0..list.size - 1) {

                when (list[i].id.toInt()) {
//                    2065, 2213, 2538 -> amenityTreeSet.add(Amenity.BUSINESS_CENTER)
                    9 -> amenityTreeSet.add(Amenity.FITNESS_CENTER)
                    371 -> amenityTreeSet.add(Amenity.HOT_TUB)
                    2046, 2097, 2100, 2101, 2125, 2126, 2127, 2156, 2191, 2192, 2220, 2390, 2392, 2394, 2403, 2405, 2407 -> amenityTreeSet.add(Amenity.INTERNET)
                    2186 -> amenityTreeSet.add(Amenity.KIDS_ACTIVITIES)
                    312, 2158, 2208 -> amenityTreeSet.add(Amenity.KITCHEN)
                    51, 2338 -> amenityTreeSet.add(Amenity.PETS_ALLOWED)
                    14, 24, 2074, 2138, 2859 -> amenityTreeSet.add(Amenity.POOL)
                    19 -> amenityTreeSet.add(Amenity.RESTAURANT)
                    2017, 2123, 2341 -> amenityTreeSet.add(Amenity.SPA)
                    361, 2001, 2077, 2098, 2102, 2103, 2104, 2105, 2193, 2194, 2205, 2209, 2210, 2211 -> amenityTreeSet.add(Amenity.BREAKFAST)
                    6 -> amenityTreeSet.add(Amenity.BABYSITTING)
                    28, 2011, 2013, 2109, 2110, 2132, 2133, 2195, 2215, 2216, 2553, 2798 -> amenityTreeSet.add(Amenity.PARKING)
                    20, 2015, 2053 -> amenityTreeSet.add(Amenity.ROOM_SERVICE)
                    2419 -> amenityTreeSet.add(Amenity.ACCESSIBLE_PATHS)
                    2420 -> amenityTreeSet.add(Amenity.ACCESSIBLE_BATHROOM)
                    2421 -> amenityTreeSet.add(Amenity.ROLL_IN_SHOWER)
                    2422 -> amenityTreeSet.add(Amenity.HANDICAPPED_PARKING)
                    2423 -> amenityTreeSet.add(Amenity.IN_ROOM_ACCESSIBILITY)
                    2424 -> amenityTreeSet.add(Amenity.DEAF_ACCESSIBILITY_EQUIPMENT)
                    2425 -> amenityTreeSet.add(Amenity.BRAILLE_SIGNAGE)
                    10, 2196, 2214, 2353 -> amenityTreeSet.add(Amenity.FREE_AIRPORT_SHUTTLE)
                }
            }

            if (amenityTreeSet.isEmpty()) return emptyList()
            return ArrayList<Amenity>(amenityTreeSet)
        }


    }
}

class AmenityComparator : Comparator<Amenity> {
    override fun compare(lhs: Amenity, rhs: Amenity): Int {
        return lhs.preference.minus(rhs.preference)
    }
}
