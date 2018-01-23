package com.expedia.bookings.hotel.data

import android.content.Context
import android.content.res.AssetManager
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.mobiata.android.util.IoUtils
import org.json.JSONObject
import java.util.ArrayList
import java.util.Comparator
import java.util.TreeSet

enum class Amenity(@DrawableRes val drawableRes: Int,
                   @StringRes val filterDescriptionId: Int?,
                   @StringRes val propertyDescriptionId: Int,
                   val priority: Int, val jsonId: String) {

    POOL(R.drawable.ic_amenity_pool, R.string.amenity_pool, R.string.amenity_pool, priority = 0, jsonId = "pool"),
    POOL_INDOOR(R.drawable.ic_amenity_pool, null, R.string.amenity_pool_indoor, priority = 0, jsonId = "poolIndoor"),
    POOL_OUTDOOR(R.drawable.ic_amenity_pool, null, R.string.amenity_pool_outdoor, priority = 0, jsonId = "poolOutdoor"),
    INTERNET(R.drawable.ic_amenity_internet, R.string.filter_high_speed_internet, R.string.amenity_internet, priority = 1, jsonId = "internet"),
    BREAKFAST(R.drawable.ic_amenity_breakfast, R.string.filter_free_breakfast, R.string.amenity_breakfast, priority = 2, jsonId = "breakfast"),
    PARKING(R.drawable.ic_amenity_local_parking, R.string.amenity_free_parking, R.string.amenity_parking, priority = 3, jsonId = "parking"),
    EXTENDED_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.amenity_parking, priority = 3, jsonId = "extendedParking"),
    FREE_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.amenity_parking, priority = 3, jsonId = "freeParking"),
    PETS(R.drawable.ic_amenity_pets, R.string.filter_pets_allowed, R.string.amenity_pets_allowed, priority = 4, jsonId = "pets"),
    RESTAURANT(R.drawable.ic_amenity_restaurant, null, R.string.amenity_restaurant, priority = 5, jsonId = "restaurant"),
    FITNESS_CENTER(R.drawable.ic_amenity_fitness_center, null, R.string.amenity_fitness_center, priority = 6, jsonId = "fitnessCenter"),
    ROOM_SERVICE(R.drawable.ic_amenity_room_service, null, R.string.amenity_room_service, priority = 7, jsonId = "roomService"),
    SPA(R.drawable.ic_amenity_spa, null, R.string.amenity_spa, priority = 8, jsonId = "spa"),
    BUSINESS_CENTER(R.drawable.ic_amenity_business_center, null, R.string.amenity_business_center, priority = 9, jsonId = "business_center"),
    AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, R.string.filter_free_airport_transportation, R.string.amenity_free_airport_shuttle, priority = 10, jsonId = "airportShuttle"),
    HOT_TUB(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_hot_tub, priority = 11, jsonId = "hotTub"),
    JACUZZI(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_jacuzzi, priority = 11, jsonId = "jacuzzi"),
    WHIRLPOOL_BATH(R.drawable.ic_amenity_hot_tub, null, R.string.amenity_whirlpool_bath, priority = 11, jsonId = "whirlpoolBath"),
    KITCHEN(R.drawable.ic_amenity_kitchen, null, R.string.amenity_kitchen, priority = 12, jsonId = "kitchen"),
    KIDS_ACTIVITIES(R.drawable.ic_amenity_kid_activities, null, R.string.amenity_kids_activities, priority = 13, jsonId = "kidsActivities"),
    BABYSITTING(R.drawable.ic_amenity_babysitting, null, R.string.amenity_babysitting, priority = 14, jsonId = "babysitting"),
    ALL_INCLUSIVE(R.drawable.ic_amenity_all_inclusive, R.string.filter_all_inclusive, R.string.amenity_all_inclusive, priority = 15, jsonId = "allInclusive"),
    AC_UNIT(R.drawable.ic_amenity_ac_unit, R.string.amenity_air_conditioning, R.string.amenity_air_conditioning, priority = 16, jsonId = "acUnit"),
    ACCESSIBLE_BATHROOM(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_bathroom, priority = 17, jsonId = "accessibleBathroom"),
    ROLL_IN_SHOWER(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_bathroom, priority = 17, jsonId = "rollInShower"),
    IN_ROOM_ACCESSIBILITY(R.drawable.ic_amenity_accessible, null, R.string.amenity_in_room_accessibility, priority = 17, jsonId = "inRoomAccessibility"),
    ACCESSIBLE_PATHS(R.drawable.ic_amenity_accessible, null, R.string.amenity_accessible_paths, priority = 18, jsonId = "accessiblePaths"),
    HANDICAPPED_PARKING(R.drawable.ic_amenity_accessible, null, R.string.amenity_handicapped_parking, priority = 18, jsonId = "handicappedParking"),
    DEAF_ACCESSIBILITY_EQUIPMENT(R.drawable.ic_amenity_accessible, null, R.string.amenity_deaf_accessibility_equipment, priority = 18, jsonId = "deafAccessibilityEquipment"),
    BRAILLE_SIGNAGE(R.drawable.ic_amenity_accessible, null, R.string.amenity_braille_signage, priority = 18, jsonId = "brailleSignage");

    companion object {
        private val AMENITY_MAP_PATH = "ExpediaSharedData/ExpediaHotelAmenityMapping.json"

        fun getFilterAmenities(): List<Amenity> {
            return listOf(Amenity.BREAKFAST, Amenity.POOL, Amenity.PARKING,
                    Amenity.PETS, Amenity.INTERNET, Amenity.AIRPORT_SHUTTLE,
                    Amenity.AC_UNIT, Amenity.ALL_INCLUSIVE)
        }

        fun getSearchKey(amenity: Amenity): Int {
            return when (amenity) {
                Amenity.BREAKFAST -> 16
                Amenity.POOL -> 7
                Amenity.PARKING -> 14
                Amenity.INTERNET -> 19
                Amenity.PETS -> 17
                Amenity.AIRPORT_SHUTTLE -> 66
                Amenity.AC_UNIT -> 27
                Amenity.ALL_INCLUSIVE -> 30
                else -> -1
            }
        }

        fun amenitiesToShow(list: List<HotelOffersResponse.HotelAmenities>, context: Context): List<Amenity> {
            val mapping = Amenity.createAmenityMap(context.assets)
            val amenityTreeSet = TreeSet<Amenity>(AmenityComparator())

            for (i in 0 until list.size) {
                val mappedAmenity = mapping[list[i].id.toInt()]
                if (mappedAmenity != null) {
                    amenityTreeSet.add(mappedAmenity)
                }
            }

            if (amenityTreeSet.isEmpty()) return emptyList()
            return ArrayList<Amenity>(amenityTreeSet)
        }

        private fun createAmenityMap(assetManager: AssetManager): Map<Int, Amenity> {
            val jsonData = JSONObject(IoUtils.convertStreamToString(assetManager.open(AMENITY_MAP_PATH)))
            val mapping = HashMap<Int, Amenity>()
            for (amenity in Amenity.values()) {
                createAmenityIdList(amenity, jsonData, mapping)
            }
            return mapping
        }

        private fun createAmenityIdList(amenity: Amenity, jsonData: JSONObject, mapping: HashMap<Int, Amenity>) {
            val jsonList = jsonData.optJSONArray(amenity.jsonId) ?: return
            for (i in 0 until jsonList.length()) {
                mapping.put(jsonList.optInt(i), amenity)
            }
        }

        private class AmenityComparator : Comparator<Amenity> {
            override fun compare(lhs: Amenity, rhs: Amenity): Int {
                return lhs.priority.minus(rhs.priority)
            }
        }
    }
}
