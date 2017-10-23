package com.expedia.bookings.hotel.util

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.google.gson.Gson


class HotelFavoriteCache {
    companion object {
        private val FAVORITE_FILE_NAME = "exp_favorite_prefs"
        private val PREFS_FAVORITE_HOTEL_IDS = "favorite_hotel_ids"
        private val PREFS_FAVORITE_HOTEL_DATA = "favorite_hotel_data_"

        fun saveHotelId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)
            favorites.add(hotelId)
            saveFavorites(context, favorites)
        }

        fun saveHotelData(context: Context, offer: HotelOffersResponse, checkIn: String, checkOut: String) {
            if (offer.hotelRoomResponse != null && offer.hotelRoomResponse.isNotEmpty()) {
                val rate = offer.hotelRoomResponse[0].rateInfo.chargeableRateInfo
                val cacheItem = HotelCacheItem(offer.hotelId, HotelPrice(rate.averageRate, rate.currencyCode), checkIn, checkOut)
                saveHotel(context, cacheItem)
            }
        }

        fun removeHotelId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)

            favorites.remove(hotelId)
            saveFavorites(context, favorites)
            removeHotelData(context, hotelId)
        }

        fun isHotelIdFavorited(context: Context, hotelId: String): Boolean {
            val favorites = getFavorites(context)
            return favorites.contains(hotelId)
        }

        fun getFavorites(context: Context): ArrayList<String> {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)

            if (settings.contains(PREFS_FAVORITE_HOTEL_IDS)) {
                val jsonFavorites = settings.getString(PREFS_FAVORITE_HOTEL_IDS, "")
                val gson = Gson()
                val favoriteItems = gson.fromJson(jsonFavorites, Array<String>::class.java)

                val favorites: List<String> = favoriteItems.toList()
                return ArrayList<String>(favorites)
            } else
                return ArrayList<String>()

        }

        fun getFavoriteHotelData(context: Context, hotelId: String) : HotelCacheItem? {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            if (settings.contains(getCacheKey(hotelId))) {
                val jsonHotel = settings.getString(getCacheKey(hotelId), null)
                val gson = Gson()
                return gson.fromJson(jsonHotel, HotelCacheItem::class.java)
            } else {
                return null
            }
        }

        private fun saveFavorites(context: Context, favorites: List<String>) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val editor = settings.edit()

            val gson = Gson()
            val jsonFavorites = gson.toJson(favorites)

            editor.putString(PREFS_FAVORITE_HOTEL_IDS, jsonFavorites)

            editor.apply()
        }

        private fun removeHotelData(context: Context, hotelId: String) {
            val editor = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.remove(getCacheKey(hotelId))
            editor.apply()
        }

        private fun saveHotel(context: Context, cacheItem: HotelCacheItem) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val editor = settings.edit()

            val gson = Gson()
            val jsonHotel = gson.toJson(cacheItem)

            editor.putString(getCacheKey(cacheItem.hotelId), jsonHotel)

            editor.apply()
        }

        private fun getCacheKey(hotelId: String): String = PREFS_FAVORITE_HOTEL_DATA + hotelId
    }

    data class HotelCacheItem(val hotelId: String, val price: HotelPrice, val checkInDate: String, val checkOutDate: String)
    data class HotelPrice(val price: Float, val currency: String)
}