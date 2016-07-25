package com.expedia.bookings.utils

import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.joda.time.LocalDate

class FlightsV2DataUtil {

    companion object {

        fun getFlightSearchParamsFromJSON(flightSearchParamsJSON: String?): FlightSearchParams? {
            val gson = generateGson()
            if (Strings.isNotEmpty(flightSearchParamsJSON) ) {
                try {
                    return gson.fromJson(flightSearchParamsJSON, FlightSearchParams::class.java)
                } catch (jse: JsonSyntaxException) {
                    throw UnsupportedOperationException()
                }
            }
            return null
        }

        fun getSuggestionFromDeeplinkLocation(deeplinkLocation: String?): SuggestionV4? {
            if (deeplinkLocation != null) {
                val suggestion = SuggestionV4()
                suggestion.gaiaId = ""
                suggestion.regionNames = SuggestionV4.RegionNames()
                suggestion.regionNames.displayName = deeplinkLocation
                suggestion.regionNames.fullName = deeplinkLocation
                suggestion.regionNames.shortName = deeplinkLocation
                suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
                suggestion.hierarchyInfo?.airport = SuggestionV4.Airport()
                suggestion.hierarchyInfo?.airport?.airportCode = deeplinkLocation
                return suggestion
            }
            return null
        }

        @JvmStatic
        fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }
    }
}
