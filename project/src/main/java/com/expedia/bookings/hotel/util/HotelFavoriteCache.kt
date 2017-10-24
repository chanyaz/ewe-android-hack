package com.expedia.bookings.hotel.util

import android.content.Context
import android.content.SharedPreferences
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.google.gson.Gson

class HotelFavoriteCache {
    companion object {
        private val FAVORITE_FILE_NAME = "exp_favorite_prefs"
        private val PREFS_FAVORITE_HOTEL_IDS = "favorite_hotel_ids"
        private val PREFS_APPWIDGET_META_DATA = "prefs_appwidget_meta_data"
        private val PREFS_FAVORITE_HOTEL_DATA = "favorite_hotel_data_"
        private val PREFS_PAST_HOTEL_DATA = "past_hotel_data_"

        private val gson = Gson()

        fun saveHotelId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)
            favorites.add(hotelId)
            saveFavorites(context, favorites)
        }

        fun saveHotelData(context: Context, offer: HotelOffersResponse) {
            if (offer.hotelRoomResponse != null && offer.hotelRoomResponse.isNotEmpty()) {
                val rate = offer.hotelRoomResponse[0].rateInfo.chargeableRateInfo

                val previousHotel = getFavoriteHotelData(context, offer.hotelId)

                val cacheItem = HotelCacheItem(offer.hotelId, offer.hotelName, HotelRate(rate.averageRate, rate.currencyCode), previousHotel?.rate, offer.hotelRoomResponse[0].currentAllotment)
                saveHotel(context, cacheItem)

                savePastData(context, offer)
            }
        }

        fun saveDates(context: Context, checkIn: String, checkOut: String) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val metaData = getAppWidgetMetaData(settings)

            metaData.checkInDate = checkIn
            metaData.checkOutDate = checkOut

            val editor = settings.edit()
            val newMetaDataJson = gson.toJson(metaData)
            editor.putString(PREFS_APPWIDGET_META_DATA, newMetaDataJson)
            editor.apply()
        }

        fun saveLastUpdateTime(context: Context, timeInMillis: Long) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val metaData = getAppWidgetMetaData(settings)
            metaData.lastUpdatedMillis = timeInMillis

            val editor = settings.edit()
            val newMetaDataJson = gson.toJson(metaData)
            editor.putString(PREFS_APPWIDGET_META_DATA, newMetaDataJson)
            editor.apply()
        }

        fun removeHotelId(context: Context, hotelId: String) {
            val favorites = getFavorites(context)

            favorites.remove(hotelId)
            saveFavorites(context, favorites)
            removeHotelData(context, hotelId)
            removePastData(context, hotelId)
        }

        fun isHotelIdFavorited(context: Context, hotelId: String): Boolean {
            val favorites = getFavorites(context)
            return favorites.contains(hotelId)
        }

        fun getFavorites(context: Context): ArrayList<String> {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)

            if (settings.contains(PREFS_FAVORITE_HOTEL_IDS)) {
                val jsonFavorites = settings.getString(PREFS_FAVORITE_HOTEL_IDS, "")
                val favoriteItems = gson.fromJson(jsonFavorites, Array<String>::class.java)

                val favorites: List<String> = favoriteItems.toList()
                return ArrayList<String>(favorites)
            } else
                return ArrayList<String>()
        }

        fun getFavoriteHotelData(context: Context, hotelId: String): HotelCacheItem? {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            if (settings.contains(getCacheKey(hotelId))) {
                val jsonHotel = settings.getString(getCacheKey(hotelId), null)
                return gson.fromJson(jsonHotel, HotelCacheItem::class.java)
            } else {
                return null
            }
        }

        fun getLastUpdated(context: Context) : Long? {
            val settings =  context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val metaData = getAppWidgetMetaData(settings)
            return metaData.lastUpdatedMillis
        }

        fun getCheckInDate(context: Context) : String? {
            val settings =  context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val metaData = getAppWidgetMetaData(settings)
            return metaData.checkInDate
        }

        fun getCheckOutDate(context: Context) : String? {
            val settings =  context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val metaData = getAppWidgetMetaData(settings)
            return metaData.checkOutDate
        }

        fun savePastData(context: Context, offer: HotelOffersResponse) {
            if (offer.hotelRoomResponse == null || offer.hotelRoomResponse.isEmpty()) {
                return
            }
            var pastData = getPastData(context, offer.hotelId)
            val rate = offer.hotelRoomResponse[0].rateInfo.chargeableRateInfo
            val pastRateToAdd = PastRate(rate.averageRate, offer.checkInDate)
            if (pastData != null) {
                val existingPastData = pastData.rates.filter { it.date == offer.checkInDate }
                if (existingPastData.count() > 0) {
                    pastData.rates.removeAll(existingPastData)
                }
                pastData.rates.add(pastRateToAdd)
                pastData.rates.sortBy { it.date }
            } else {
                val rates = ArrayList<PastRate>()
                rates.add(pastRateToAdd)

                pastData = HotelPastData(offer.hotelId, offer.hotelName, rate.currencyCode, rates)
            }

            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val editor = settings.edit()

            val jsonPastData = gson.toJson(pastData)

            editor.putString(getPastDataKey(offer.hotelId), jsonPastData)

            editor.apply()
        }

        fun removePastData(context: Context, hotelId: String) {
            val editor = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.remove(getPastDataKey(hotelId))
            editor.apply()
        }

        fun getPastData(context: Context, hotelId: String): HotelPastData? {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            if (settings.contains(getPastDataKey(hotelId))) {
                val jsonPastData = settings.getString(getPastDataKey(hotelId), null)
                val gson = Gson()
                return gson.fromJson(jsonPastData, HotelPastData::class.java)
            } else {
                return null
            }
        }

        private fun saveFavorites(context: Context, favorites: List<String>) {
            val settings = context.getSharedPreferences(FAVORITE_FILE_NAME, Context.MODE_PRIVATE)
            val editor = settings.edit()

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

            val jsonHotel = gson.toJson(cacheItem)

            editor.putString(getCacheKey(cacheItem.hotelId), jsonHotel)

            editor.apply()
        }

        private fun getCacheKey(hotelId: String): String = PREFS_FAVORITE_HOTEL_DATA + hotelId
        private fun getPastDataKey(hotelId: String): String = PREFS_PAST_HOTEL_DATA + hotelId

        private fun getAppWidgetMetaData(settings: SharedPreferences) : AppWidgetMetaData {
            val metaDataJson = settings.getString(PREFS_APPWIDGET_META_DATA, "")
            return gson.fromJson(metaDataJson, AppWidgetMetaData::class.java) ?: AppWidgetMetaData(null, null, null)
        }
    }

    data class HotelCacheItem(val hotelId: String, val hotelName: String, val rate: HotelRate,
                              val oldRate: HotelRate?, val roomsLeft: String)
    data class HotelRate(val amount: Float, val currency: String)
    data class AppWidgetMetaData(var checkInDate: String?, var checkOutDate: String?, var lastUpdatedMillis: Long?)
    data class HotelPastData(val hotelId: String, val hotelName: String, val currency: String, val rates: ArrayList<PastRate>)
    data class PastRate(val amount: Float, val date: String)
}
