package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.joda.time.LocalDate

class DeeplinkSharedPrefParserUtils {

    companion object {
        val DEEPLINK_SHARED_PREFERENCE_KEY = "com.expedia.bookings.utils.DeeplinkSharedPrefParserUtils"

        val PACKAGE_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.PACKAGE_SEARCH_PARAMS"

        val PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_SELECTED"

        val PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_ROOM_SELECTED"


        val HOTEL_SEARCH_PARAMS_TYPE = object : TypeToken<HotelSearchParams>() {}.type

        val HOTEL_ROOM_SELECTED_TYPE = object : TypeToken<HotelRoomSelectionParams>() {}.type

        val HOTEL_SELECTED_TYPE = object : TypeToken<HotelSelectionParams>() {}.type

        val LOCAL_DATE_TYPE = LocalDateTypeAdapter("dd/MM/yyyy")


        fun saveHotelSearchDeeplinkParams(hotelSearchParams: HotelSearchParams, context: Context) {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val toJson = gson.toJson(hotelSearchParams, HOTEL_SEARCH_PARAMS_TYPE)

             val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun saveHotelRoomSelectionParams(hotelRoomSelectionParams: HotelRoomSelectionParams, context: Context) {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val toJson = gson.toJson(hotelRoomSelectionParams, HOTEL_ROOM_SELECTED_TYPE)

            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun saveHotelSelectionParams(hotelSelectionParams: HotelSelectionParams, context: Context) {
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val toJson = gson.toJson(hotelSelectionParams, HOTEL_SELECTED_TYPE)

            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

            bookmarksSharedPref.edit().putString(PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY, toJson).apply()
        }

        fun getHotelSearchDeeplinkParams(context: Context): HotelSearchParams? {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val json = bookmarksSharedPref.getString(PACKAGE_SEARCH_PARAMS_KEY, "")
            if (json.isEmpty()) {
                return null
            }
            bookmarksSharedPref.edit().remove(PACKAGE_SEARCH_PARAMS_KEY).apply()

            return gson.fromJson<HotelSearchParams>(json, HOTEL_SEARCH_PARAMS_TYPE)
        }


        fun getHotelSelectionDeeplinkParams(context: Context): HotelSelectionParams? {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val json = bookmarksSharedPref.getString(PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY, "")
            if (json.isEmpty()) {
                return null
            }

            bookmarksSharedPref.edit().remove(PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY).apply()

            return gson.fromJson<HotelSelectionParams>(json, HOTEL_SELECTED_TYPE)
        }

        fun getHotelRoomSelectionDeeplinkParams(context: Context): HotelRoomSelectionParams? {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val json = bookmarksSharedPref.getString(PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY, "")
            if (json.isEmpty()) {
                return null
            }

            bookmarksSharedPref.edit().remove(PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY).apply()

            return gson.fromJson<HotelRoomSelectionParams>(json, HOTEL_ROOM_SELECTED_TYPE)
        }
    }

}
