package com.expedia.bookings.hotel.util

import android.content.Context

class HotelFavoritesCache {
    companion object {
        const val FAVORITES_FILE_NAME = "hotel_favorites_prefs"
        private val FAVORITE_HOTEL_IDS = "hotel_favorites_ids"

        fun saveFavoriteId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)
            favorites.add(hotelId)
            saveFavorites(context, favorites)
        }

        fun removeFavoriteId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)
            favorites.remove(hotelId)
            saveFavorites(context, favorites)
        }

        fun isFavoriteHotel(context: Context, hotelId: String): Boolean {
            val favorites = getFavorites(context)
            return favorites.contains(hotelId)
        }

        fun saveFavorites(context: Context, favorites: Set<String>) {
            val prefs = context.getSharedPreferences(FAVORITES_FILE_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putStringSet(FAVORITE_HOTEL_IDS, favorites)
            editor.apply()
        }

        fun clearFavorites(context: Context) {
            val prefs = context.getSharedPreferences(FAVORITES_FILE_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
        }

        private fun getFavorites(context: Context): MutableSet<String> {
            val prefs = context.getSharedPreferences(FAVORITES_FILE_NAME, Context.MODE_PRIVATE)
            return (prefs.getStringSet(FAVORITE_HOTEL_IDS, HashSet<String>())).toMutableSet()
        }
    }
}
