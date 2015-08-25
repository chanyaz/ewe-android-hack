package com.expedia.bookings.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.larvalabs.svgandroid.widget.SVGView
import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap
import java.util.TreeSet
import kotlin.properties.Delegates

/**
 * Created by mohsharma on 8/12/15.
 */

// preference number tells order we want to show in hotel details. if we change the preference number it will change.
enum class Amenity(val preference: Int, val resId: Int, val strId: Int) {
    POOL(0, R.raw.ic_amenity_pool, R.string.AmenityPool),
    POOL_INDOOR(0, R.raw.ic_amenity_pool, R.string.AmenityPoolIndoor),
    POOL_OUTDOOR(0, R.raw.ic_amenity_pool, R.string.AmenityPoolOutdoor),
    INTERNET(1, R.raw.ic_amenity_internet, R.string.AmenityInternet),
    BREAKFAST(2, R.raw.ic_amenity_breakfast, R.string.AmenityBreakfast),
    PARKING(3, R.raw.ic_amenity_parking, R.string.AmenityParking),
    EXTENDED_PARKING(3, R.raw.ic_amenity_parking, R.string.AmenityParking),
    FREE_PARKING(3, R.raw.ic_amenity_parking, R.string.AmenityParking),
    PETS_ALLOWED(4, R.raw.ic_amenity_pets, R.string.AmenityPetsAllowed),
    RESTAURANT(5, R.raw.ic_amenity_restaurant, R.string.AmenityRestaurant),
    FITNESS_CENTER(6, R.raw.ic_amenity_fitness_center, R.string.AmenityFitnessCenter),
    ROOM_SERVICE(7, R.raw.ic_amenity_room_service, R.string.AmenityRoomService),
    SPA(8, R.raw.ic_amenity_spa, R.string.AmenitySpa),
    BUSINESS_CENTER(9, R.raw.ic_amenity_business, R.string.AmenityBusinessCenter),
    FREE_AIRPORT_SHUTTLE(10, R.raw.ic_amenity_airport_shuttle, R.string.AmenityFreeAirportShuttle),
    ACCESSIBLE_BATHROOM(11, R.raw.ic_amenity_accessible_bathroom, R.string.AmenityAccessibleBathroom),
    HOT_TUB(12, R.raw.ic_amenity_hot_tub, R.string.AmenityHotTub),
    JACUZZI(13, R.raw.ic_amenity_jacuzzi, R.string.AmenityJacuzzi),
    WHIRLPOOL_BATH(14, R.raw.ic_amenity_whirl_pool, R.string.AmenityWhirlpoolBath),
    KITCHEN(15, R.raw.ic_amenity_kitchen, R.string.AmenityKitchen),
    KIDS_ACTIVITIES(16, R.raw.ic_amenity_children_activities, R.string.AmenityKidsActivities),
    BABYSITTING(17, R.raw.ic_amenity_baby_sitting, R.string.AmenityBabysitting),
    ACCESSIBLE_PATHS(18, R.raw.ic_amenity_accessible_ramp, R.string.AmenityAccessiblePaths),
    ROLL_IN_SHOWER(19, R.raw.ic_amenity_accessible_shower, R.string.AmenityAccessibleBathroom),
    HANDICAPPED_PARKING(20, R.raw.ic_amenity_handicap_parking, R.string.AmenityHandicappedParking),
    IN_ROOM_ACCESSIBILITY(21, R.raw.ic_amenity_accessible_room, R.string.AmenityInRoomAccessibility),
    DEAF_ACCESSIBILITY_EQUIPMENT(22, R.raw.ic_amenity_deaf_access, R.string.AmenityDeafAccessibilityEquipment),
    BRAILLE_SIGNAGE(23, R.raw.ic_amenity_braille_signs, R.string.AmenityBrailleSignage);


    // static helper method to add amenities
    companion object {

        fun addAmenity(viewGroup: ViewGroup, amenityList: List<Amenity>) {
            viewGroup.removeAllViews()
            val srcColor = viewGroup.getContext().getResources().getColor(R.color.amenity_icon_color)
            val mode = PorterDuff.Mode.SRC_ATOP
            val filter = PorterDuffColorFilter(srcColor, mode)
            val paint = Paint()
            paint.setColorFilter(filter)

            val MAX_AMENITY_TEXT_WIDTH_IN_DP = 60.0f;
            val acceptableWidth = viewGroup.getContext().getResources().getDisplayMetrics().density * MAX_AMENITY_TEXT_WIDTH_IN_DP

            for (index in 0..amenityList.size() - 1) {

                val amenityLayout = com.mobiata.android.util.Ui.inflate<LinearLayout>(R.layout.snippet_amenity, viewGroup, false);
                val amenityTextView = amenityLayout.findViewById(R.id.label) as widget.TextView
                val amenityIconView = amenityLayout.findViewById(R.id.icon) as SVGView
                amenityIconView.setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
                val amenityStr = viewGroup.getContext().getString(amenityList.get(index).strId)
                val measuredWidthOfStr = amenityTextView.getPaint().measureText(viewGroup.getContext().getString(amenityList.get(index).strId))
                if (amenityStr.contains(" ") || measuredWidthOfStr > acceptableWidth) {
                    amenityTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewGroup.getContext().getResources().getDimension(R.dimen.amenity_text_size_small))
                }

                amenityTextView.setText(amenityStr)
                amenityIconView.setSVG(amenityList.get(index).resId)
                viewGroup.addView(amenityLayout)
            }
            viewGroup.scheduleLayoutAnimation()

        }

        public fun amenitiesToShow(list: List<HotelOffersResponse.HotelAmenities>): List<Amenity> {

            var amenityTreeSet = TreeSet<Amenity>(AmenityComparator())

            for (i in 0..list.size() - 1) {

                when (list.get(i).id.toInt()) {
                    2065, 2213, 2538 -> amenityTreeSet.add(Amenity.BUSINESS_CENTER)
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
