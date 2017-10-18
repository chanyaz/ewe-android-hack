package com.expedia.bookings.hotel.util

import android.content.Context
import com.google.gson.Gson


class HotelFavoriteCache {
    companion object {
        private val FAVORITE_FILE_NAME = "exp_favorite_prefs"
        private val PREFS_FAVORITE_HOTEL_IDS = "proWizardBucket"

        fun saveHotelId(context: Context, hotelId: String) {
            var favorites = getFavorites(context)
            if (favorites == null) {
                favorites = ArrayList<String>()
            }
            favorites.add(hotelId)
            saveFavorites(context, favorites)
        }

        fun removeHotelId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)
            if (favorites != null) {
                favorites.remove(hotelId)
                saveFavorites(context, favorites)
            }
        }

        fun isHotelIdFavorited(context: Context, hotelId: String) : Boolean {
            val favorites = getFavorites(context)
            return favorites?.contains(hotelId) ?: false
        }

        fun getFavorites(context: Context): ArrayList<String>? {
            var favorites: List<String>

            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)

            if (settings.contains(PREFS_FAVORITE_HOTEL_IDS)) {
                val jsonFavorites = settings.getString(PREFS_FAVORITE_HOTEL_IDS, null)
                val gson = Gson()
                val favoriteItems = gson.fromJson(jsonFavorites, Array<String>::class.java)

                favorites = favoriteItems.toList()
                favorites = ArrayList<String>(favorites)
            } else
                return null

            return favorites
        }

        private fun saveFavorites(context: Context, favorites: List<String>) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val editor = settings.edit()

            val gson = Gson()
            val jsonFavorites = gson.toJson(favorites)

            editor.putString(PREFS_FAVORITE_HOTEL_IDS, jsonFavorites)

            editor.apply()
        }
    }
}