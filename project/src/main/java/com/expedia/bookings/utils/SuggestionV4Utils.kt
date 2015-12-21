package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.hotels.SuggestionV4
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

public object SuggestionV4Utils {

    public val RECENT_HOTEL_SUGGESTIONS_FILE = "recent-hotel-suggest-list.dat"
    public val RECENT_PACKAGE_SUGGESTIONS_FILE = "recent-package-suggest-list.dat"

    public fun saveSuggestionHistory(context: Context, suggestion: SuggestionV4, file: String) {
        Thread(object : Runnable {
            override fun run() {
                val suggest = suggestion.copy()
                if(suggest.type == "RAW_TEXT_SEARCH") {
                    return // don't store raw (non-ESS) searches
                }

                if (suggest.regionNames.displayName == context.getString(com.expedia.bookings.R.string.current_location)) {
                    suggest.regionNames.displayName = suggest.regionNames.shortName
                }
                suggest.hierarchyInfo?.isChild = false
                val suggestions = listOf(suggest) + loadSuggestionHistory(context, file)
                val recentSuggestions = suggestions.distinctBy { it.gaiaId ?: it.regionNames.displayName}

                val type = object : TypeToken<List<SuggestionV4>>() { }.type
                val suggestionJson = Gson().toJson(recentSuggestions.take(3), type)
                try {
                    IoUtils.writeStringToFile(file, suggestionJson, context)
                } catch (e: IOException) {
                    Log.e("Save History Error: ", e)
                }
            }
        }).start()
    }

    public fun loadSuggestionHistory(context: Context, file: String): List<SuggestionV4> {
        var recentSuggestions = emptyList<SuggestionV4>()
        try {
            val str = IoUtils.readStringFromFile(file, context)
            val type = object : TypeToken<List<SuggestionV4>>() { }.type
            recentSuggestions = Gson().fromJson<List<SuggestionV4>>(str, type)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        recentSuggestions.forEach { it.iconType = SuggestionV4.IconType.HISTORY_ICON }

        return recentSuggestions.toArrayList()
    }

    @JvmStatic public fun deleteCachedSuggestions(context: Context) {
        val locationFiles = arrayOf(RECENT_HOTEL_SUGGESTIONS_FILE, RECENT_PACKAGE_SUGGESTIONS_FILE)
        for (locationFile in locationFiles) {
            val file = context.getFileStreamPath(locationFile)
            val fileExists = file.exists()
            val isDeleted = file.delete()
            if (fileExists && !isDeleted) {
                Log.e("Unable to delete suggestion history in " + locationFile)
            }
        }
    }
}
