package com.expedia.bookings.hotel.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R

enum class HotelAmenity(@DrawableRes val drawableRes: Int,
                        @StringRes val filterDescriptionId: Int?,
                        @StringRes val propertyDescriptionId: Int,
                        val priority: Int) {

    POOL(R.drawable.ic_amenity_pool, R.string.AmenityPool, R.string.AmenityPool, priority = 0),
    POOL_INDOOR(R.drawable.ic_amenity_pool, null, R.string.AmenityPoolIndoor, priority = 0),
    POOL_OUTDOOR(R.drawable.ic_amenity_pool, null, R.string.AmenityPoolOutdoor, priority = 0),
    INTERNET(R.drawable.ic_amenity_internet, R.string.filter_high_speed_internet, R.string.AmenityInternet, priority = 1),
    BREAKFAST(R.drawable.ic_amenity_breakfast, R.string.filter_free_breakfast, R.string.AmenityBreakfast, priority = 2),
    PARKING(R.drawable.ic_amenity_local_parking, R.string.AmenityFreeParking, R.string.AmenityParking, priority = 3),
    EXTENDED_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.AmenityParking, priority = 3),
    FREE_PARKING(R.drawable.ic_amenity_local_parking, null, R.string.AmenityParking, priority = 3),
    PETS(R.drawable.ic_amenity_pets, R.string.filter_pets_allowed, R.string.AmenityPetsAllowed, priority = 4),
    RESTAURANT(R.drawable.ic_amenity_restaurant, null, R.string.AmenityRestaurant, priority = 5),
    FITNESS_CENTER(R.drawable.ic_amenity_fitness_center, null, R.string.AmenityFitnessCenter, priority = 6),
    ROOM_SERVICE(R.drawable.ic_amenity_room_service, null, R.string.AmenityRoomService, priority = 7),
    SPA(R.drawable.ic_amenity_spa, null, R.string.AmenitySpa, priority = 8),
    BUSINESS_CENTER(R.drawable.ic_amenity_business_center, null, R.string.AmenityBusinessCenter, priority = 9),
    AIRPORT_SHUTTLE(R.drawable.ic_amenity_airport_shuttle, R.string.filter_free_airport_transportation, R.string.AmenityFreeAirportShuttle, priority = 10),
    HOT_TUB(R.drawable.ic_amenity_hot_tub, null, R.string.AmenityHotTub, priority = 11),
    JACUZZI(R.drawable.ic_amenity_hot_tub, null, R.string.AmenityJacuzzi, priority = 11),
    WHIRLPOOL_BATH(R.drawable.ic_amenity_hot_tub, null, R.string.AmenityWhirlpoolBath, priority = 11),
    KITCHEN(R.drawable.ic_amenity_kitchen, null, R.string.AmenityKitchen, priority = 12),
    KIDS_ACTIVITIES(R.drawable.ic_amenity_kid_activities, null, R.string.AmenityKidsActivities, priority = 13),
    BABYSITTING(R.drawable.ic_amenity_babysitting, null, R.string.AmenityBabysitting, priority = 14),
    ALL_INCLUSIVE(R.drawable.ic_amenity_all_inclusive, R.string.filter_all_inclusive, R.string.AmenityAllInclusive, priority = 15),
    AC_UNIT(R.drawable.ic_amenity_ac_unit, R.string.AmenityAirConditioning, R.string.AmenityAirConditioning, priority = 16),
    ACCESSIBLE_BATHROOM(R.drawable.ic_amenity_accessible, null, R.string.AmenityAccessibleBathroom, priority = 17),
    ROLL_IN_SHOWER(R.drawable.ic_amenity_accessible, null, R.string.AmenityAccessibleBathroom, priority = 17),
    IN_ROOM_ACCESSIBILITY(R.drawable.ic_amenity_accessible, null, R.string.AmenityInRoomAccessibility, priority = 17),
    ACCESSIBLE_PATHS(R.drawable.ic_amenity_accessible, null, R.string.AmenityAccessiblePaths, priority = 18),
    HANDICAPPED_PARKING(R.drawable.ic_amenity_accessible, null, R.string.AmenityHandicappedParking,  priority = 18),
    DEAF_ACCESSIBILITY_EQUIPMENT(R.drawable.ic_amenity_accessible, null, R.string.AmenityDeafAccessibilityEquipment,  priority = 18),
    BRAILLE_SIGNAGE(R.drawable.ic_amenity_accessible, null, R.string.AmenityBrailleSignage,  priority = 18);
}
