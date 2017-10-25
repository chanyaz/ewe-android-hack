package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.joda.time.LocalDate
import java.util.ArrayList
import android.R.id.edit



class DeeplinkSharedPrefParserUtils {

    companion object {
        val DEEPLINK_SHARED_PREFERENCE_KEY = "com.expedia.bookings.utils.DeeplinkSharedPrefParserUtils"

        val PACKAGE_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.PACKAGE_SEARCH_PARAMS"

        val PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_SELECTED"

        val PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY = "PACKAGES_REPLAY.HOTEL_ROOM_SELECTED"

        val PACKAGES_FLIGHT_INBOUND_PARAMS = "PACKAGES_REPLAY.FLIGHT_INBOUND_PARAMS"

        val PACKAGES_FLIGHT_OUTBOUND_PARAMS = "PACKAGES_REPLAY.FLIGHT_OUTBOUND_PARAMS"

        val HOTEL_SEARCH_PARAMS_TYPE = object : TypeToken<HotelSearchParams>() {}.type

        val HOTEL_ROOM_SELECTED_TYPE = object : TypeToken<HotelRoomSelectionParams>() {}.type

        val HOTEL_SELECTED_TYPE = object : TypeToken<HotelSelectionParams>() {}.type

        val FLIGHT_INBOUND_SELECTION_PARAMS_TYPE = object : TypeToken<List<FlightInboundParams>>() {}.type

        val FLIGHT_OUTBOUND_SELECTION_PARAMS_TYPE = object : TypeToken<List<FlightOutboundParams>>() {}.type

        val LOCAL_DATE_TYPE = LocalDateTypeAdapter("dd/MM/yyyy")

        val BOOKING_IS_INCOMPLETE = "booking_is_incomplete"


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
                isDeeplink = false
                return null
            }
            bookmarksSharedPref.edit().remove(PACKAGE_SEARCH_PARAMS_KEY).apply()
            isDeeplink = true

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

        var isDeeplink: Boolean = false

        fun saveInboundFlightSelectionParams(flightInboundParamList: ArrayList<FlightInboundParams>, context: Context) {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            bookmarksSharedPref.edit().putString(PACKAGES_FLIGHT_INBOUND_PARAMS, gson.toJson(flightInboundParamList, FLIGHT_INBOUND_SELECTION_PARAMS_TYPE)).apply()
        }

        fun saveOutboundFlightSelectionParams(flightOutboundParamList: ArrayList<FlightOutboundParams>, context: Context) {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            bookmarksSharedPref.edit().putString(PACKAGES_FLIGHT_OUTBOUND_PARAMS, gson.toJson(flightOutboundParamList, FLIGHT_OUTBOUND_SELECTION_PARAMS_TYPE)).apply()
        }

        fun getInboundFlightSelectionParams(context: Context): List<FlightInboundParams>? {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val json = bookmarksSharedPref.getString(PACKAGES_FLIGHT_INBOUND_PARAMS, "")
            if (json.isEmpty()) {
                return null
            }

            bookmarksSharedPref.edit().remove(PACKAGES_FLIGHT_INBOUND_PARAMS).apply()

            return gson.fromJson<List<FlightInboundParams>>(json, FLIGHT_INBOUND_SELECTION_PARAMS_TYPE)
        }

        fun getOutboundFlightSelectionParams(context: Context): List<FlightOutboundParams>? {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LOCAL_DATE_TYPE).create()
            val json = bookmarksSharedPref.getString(PACKAGES_FLIGHT_OUTBOUND_PARAMS, "")
            if (json.isEmpty()) {
                return null
            }

            bookmarksSharedPref.edit().remove(PACKAGES_FLIGHT_OUTBOUND_PARAMS).apply()

            return gson.fromJson<List<FlightOutboundParams>>(json, FLIGHT_OUTBOUND_SELECTION_PARAMS_TYPE)
        }

        fun removeSharedPref(context: Context) {
            val bookmarksSharedPref = context.getSharedPreferences(DeeplinkSharedPrefParserUtils.DEEPLINK_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)
            bookmarksSharedPref.edit().remove(PACKAGES_FLIGHT_INBOUND_PARAMS).apply()
            bookmarksSharedPref.edit().remove(PACKAGES_FLIGHT_OUTBOUND_PARAMS).apply()
            bookmarksSharedPref.edit().remove(PACKAGE_SEARCH_PARAMS_KEY).apply()
            bookmarksSharedPref.edit().remove(PACKAGE_HOTEL_SELECTED_SEARCH_PARAMS_KEY).apply()
            bookmarksSharedPref.edit().remove(PACKAGE_HOTEL_ROOM_SELECTED_SEARCH_PARAMS_KEY).apply()
            isDeeplink = false
        }

    }

}
