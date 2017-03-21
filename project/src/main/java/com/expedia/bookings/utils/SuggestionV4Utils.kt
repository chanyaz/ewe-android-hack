package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import rx.subjects.PublishSubject
import java.io.IOException
import java.util.ArrayList

object SuggestionV4Utils {

    val RECENT_HOTEL_SUGGESTIONS_FILE = "recent-hotel-suggest-list.dat"
    val RECENT_PACKAGE_SUGGESTIONS_FILE = "recent-package-suggest-list.dat"
    val RECENT_AIRPORT_SUGGESTIONS_FILE = "recent-airport-suggest-list.dat"
    val RECENT_CAR_SUGGESTIONS_FILE = "recent-cars-airport-routes-list-v4.dat"
    val RECENT_RAIL_SUGGESTIONS_FILE = "recent-rail-suggest-list.dat"
    val RECENT_LX_SUGGESTIONS_FILE =  "recent-lx-city-list-v4.dat"

    val testSuggestionSavedSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

    fun saveSuggestionHistory(context: Context, suggestion: SuggestionV4, file: String) {
        Thread(Runnable {
            val suggest = suggestion.copy()
            if (suggest.type == "RAW_TEXT_SEARCH") {
                return@Runnable // don't store raw (non-ESS) searches
            }

            if (suggest.regionNames.displayName == context.getString(com.expedia.bookings.R.string.current_location)) {
                suggest.regionNames.displayName = suggest.regionNames.shortName
            }
            val suggestions = listOf(suggest) + loadSuggestionHistory(context, file)
            val recentSuggestions = suggestions.distinctBy { it.coordinates.lat }

            val type = object : TypeToken<List<SuggestionV4>>() {}.type
            val suggestionJson = Gson().toJson(recentSuggestions.take(3), type)
            try {
                IoUtils.writeStringToFile(file, suggestionJson, context)
                testSuggestionSavedSubject.onNext(Unit)
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

    fun convertToSuggestionV4(gaiaSuggestions: List<GaiaSuggestion>): MutableList<SuggestionV4> {
        val suggestionList = ArrayList<SuggestionV4>()
        gaiaSuggestions.forEach { it ->
            val suggestion = SuggestionV4();
            suggestion.gaiaId = it.gaiaID;
            suggestion.type = it.type;

            val latlong = SuggestionV4.LatLng()
            latlong.lat = it.latLong.latitude
            latlong.lng = it.latLong.longitude
            suggestion.coordinates = latlong

            val regionName = SuggestionV4.RegionNames();
            val localizedNames = it.localizedNames.get(0)
            regionName.fullName = localizedNames.fullName
            regionName.displayName = StrUtils.getDisplayNameForGaiaNearby(localizedNames.friendlyName, SuggestionStrUtils.formatAirportName(localizedNames.airportName))
            regionName.shortName = localizedNames.shortName
            suggestion.regionNames = regionName

            val hierarchyInfo = SuggestionV4.HierarchyInfo()
            val country = SuggestionV4.Country()
            val airport = SuggestionV4.Airport()
            country.name = it.country.name
            country.countryCode = it.country.code
            hierarchyInfo.country = country

            if (it.airportCode != null) {
                airport.airportCode = it.airportCode
            }
            if (it.regionId != null) {
                airport.multicity = it.regionId!!.first().id
            }
            hierarchyInfo.airport = airport

            suggestion.hierarchyInfo = hierarchyInfo

            suggestion.isMinorAirport = !it.isMajorAirport
            suggestionList.add(suggestion)
        }
        return suggestionList.toMutableList()
    }

    @JvmStatic fun deleteCachedSuggestions(context: Context) {
        val locationFiles = arrayOf(RECENT_HOTEL_SUGGESTIONS_FILE, RECENT_PACKAGE_SUGGESTIONS_FILE, RECENT_AIRPORT_SUGGESTIONS_FILE, RECENT_LX_SUGGESTIONS_FILE, RECENT_CAR_SUGGESTIONS_FILE)
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
