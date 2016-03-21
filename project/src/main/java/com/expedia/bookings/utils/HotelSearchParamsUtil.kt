package com.expedia.bookings.utils;

import android.content.Context
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import org.joda.time.LocalDate
import java.io.IOException
import java.util.ArrayList

object HotelSearchParamsUtil {
    val RECENT_HOTEL_SEARCHES_FILE = "recent-hotel-search-list.dat"
    val PATTERN = "yyyy-MM-dd"

    fun saveSearchHistory(context: Context, searchParams: HotelSearchParams) {
        Thread(object : Runnable {
            override fun run() {

                if (searchParams.suggestion.type == "RAW_TEXT_SEARCH") {
                    return // don't store raw (non-ESS) searches
                }
                if (searchParams.suggestion.isCurrentLocationSearch) {
                    searchParams.suggestion.regionNames.displayName = searchParams.suggestion.regionNames.shortName
                }
                if (null == searchParams.suggestion.regionNames.displayName ) {
                    searchParams.suggestion.regionNames.displayName = searchParams.suggestion.regionNames.fullName
                }

                val type = object : TypeToken<List<HotelSearchParams>>() {}.type
                var savedSearches = loadSearchHistory(context)
                savedSearches.add(0, searchParams)
                val recentSearches = savedSearches.distinctBy { listOf(it.suggestion.gaiaId, it.suggestion.regionNames.displayName, it.checkIn, it.checkOut, it.adults, it.children.size) }

                val builder = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN))
                val searchJson = builder.create().toJson(recentSearches.take(10), type)
                try {
                    IoUtils.writeStringToFile(RECENT_HOTEL_SEARCHES_FILE, searchJson, context)
                } catch (e: IOException) {
                    Log.e("Save search history error: ", e)
                }
            }
        }).start()
    }

    fun loadSearchHistory(context: Context): ArrayList<HotelSearchParams> {
        try {
            val str = IoUtils.readStringFromFile(RECENT_HOTEL_SEARCHES_FILE, context)
            val type = object : TypeToken<List<HotelSearchParams>>() {}.type

            val builder = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN))
            val recentSearches = builder.create().fromJson<List<HotelSearchParams>>(str, type)
                    .filter { JodaUtils.isBeforeOrEquals(LocalDate.now(), it.checkIn) }
            return recentSearches.toCollection(ArrayList())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return emptyList<HotelSearchParams>().toCollection(ArrayList())
    }

    @JvmStatic fun deleteCachedSearches(context: Context) {
        val locationFiles = arrayOf(RECENT_HOTEL_SEARCHES_FILE)
        for (locationFile in locationFiles) {
            val file = context.getFileStreamPath(locationFile)
            val fileExists = file.exists()
            val isDeleted = file.delete()
            if (fileExists && !isDeleted) {
                Log.e("Unable to delete search history in " + locationFile)
            }
        }
    }
}
