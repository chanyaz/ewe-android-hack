package com.expedia.bookings.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DeeplinkSharedPrefParserUtils {

    companion object {
        val DEEPLINK_SHARED_PREFERENCE_KEY = "com.expedia.bookings.utils.DeeplinkSharedPrefParserUtils"

        val PACKAGE_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.PACKAGE_SEARCH_PARAMS"

        val PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_SELECTED"

        val PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_ROOM_SELECTED"


        val HOTEL_SEARCH_PARAMS_TYPE = object : TypeToken<HotelSearchParams>() {}.type

        val HOTEL_ROOM_SELECTED_TYPE = object : TypeToken<HotelRoomSelectionParams>() {}.type

        val HOTEL_SELECTED_TYPE = object : TypeToken<HotelSelectionParams>() {}.type


        fun saveHotelSearchDeeplinkParams(hotelSearchParams: HotelSearchParams, context: Context) {

            val gson = Gson()
            val toJson = gson.toJson(hotelSearchParams, HOTEL_SEARCH_PARAMS_TYPE)

            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun saveHotelRoomSelectionParams(hotelRoomSelectionParams: HotelRoomSelectionParams, context: Context) {
            val gson = Gson()
            val toJson = gson.toJson(hotelRoomSelectionParams, HOTEL_ROOM_SELECTED_TYPE)

            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun saveHotelSelectionParams(hotelSelectionParams: HotelSelectionParams, context: Context) {
            val gson = Gson()
            val toJson = gson.toJson(hotelSelectionParams, HOTEL_SELECTED_TYPE)

            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun getHotelSearchDeeplinkParams(context: Context){

        }
    }

}
