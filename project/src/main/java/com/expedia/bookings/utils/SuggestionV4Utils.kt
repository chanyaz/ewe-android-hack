package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

object SuggestionV4Utils {

    val RECENT_HOTEL_SUGGESTIONS_FILE = "recent-hotel-suggest-list.dat"
    val RECENT_PACKAGE_SUGGESTIONS_FILE = "recent-package-suggest-list.dat"
    val RECENT_AIRPORT_SUGGESTIONS_FILE = "recent-airport-suggest-list.dat"

    fun saveSuggestionHistory(context: Context, suggestion: SuggestionV4, file: String) {
        Thread(Runnable {
            val suggest = suggestion.copy()
            if (suggest.type == "RAW_TEXT_SEARCH") {
                return@Runnable // don't store raw (non-ESS) searches
            }

            if (suggest.regionNames.displayName == context.getString(com.expedia.bookings.R.string.current_location)) {
                suggest.regionNames.displayName = suggest.regionNames.shortName
            }
            suggest.hierarchyInfo?.isChild = false
            val suggestions = listOf(suggest) + loadSuggestionHistory(context, file)
            val recentSuggestions = suggestions.distinctBy { it.gaiaId ?: it.regionNames.displayName }

            val type = object : TypeToken<List<SuggestionV4>>() {}.type
            val suggestionJson = Gson().toJson(recentSuggestions.take(3), type)
            try {
                IoUtils.writeStringToFile(file, suggestionJson, context)
            } catch (e: IOException) {
                Log.e("Save History Error: ", e)
            }
        }).start()
    }

    fun loadSuggestionHistory(context: Context, file: String): List<SuggestionV4> {
        var recentSuggestions = emptyList<SuggestionV4>()
        try {
            val str = IoUtils.readStringFromFile(file, context)
            val type = object : TypeToken<List<SuggestionV4>>() {}.type
            recentSuggestions = Gson().fromJson<List<SuggestionV4>>(str, type)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        recentSuggestions.forEach { it.iconType = SuggestionV4.IconType.HISTORY_ICON }

        return recentSuggestions.toMutableList()
    }

    @JvmStatic fun deleteCachedSuggestions(context: Context) {
        val locationFiles = arrayOf(RECENT_HOTEL_SUGGESTIONS_FILE, RECENT_PACKAGE_SUGGESTIONS_FILE, RECENT_AIRPORT_SUGGESTIONS_FILE)
        for (locationFile in locationFiles) {
            val file = context.getFileStreamPath(locationFile)
            val fileExists = file.exists()
            val isDeleted = file.delete()
            if (fileExists && !isDeleted) {
                Log.e("Unable to delete suggestion history in " + locationFile)
            }
        }
    }

    /**
     * Get the minimum number of characters required to provide drop down auto fill results.
     * This is useful for languages like Japanese where Tokyo is spelt with 2 characters.
     * Used for languages: Chinese, Korean, and Japanese

     * @return min number of characters to start a query
     */
    @JvmStatic fun getMinSuggestQueryLength(context: Context): Int {
        return context.resources.getInteger(R.integer.suggest_min_query_length)
    }
}
