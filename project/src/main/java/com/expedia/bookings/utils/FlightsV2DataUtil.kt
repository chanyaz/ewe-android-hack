package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.expedia.bookings.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.mobiata.flightlib.data.Airport
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

        fun getSuggestionV4FromAirport(context: Context, airport: Airport): SuggestionV4 {
            val airportSuggestion = SuggestionV4()
            airportSuggestion.regionNames = SuggestionV4.RegionNames()
            val airportName = HtmlCompat.stripHtml(context.getString(R.string.dropdown_airport_selection, airport.mAirportCode, airport.mName))
            airportSuggestion.regionNames.displayName = airportName
            airportSuggestion.regionNames.shortName = airportName
            airportSuggestion.regionNames.fullName = airportName
            airportSuggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
            airportSuggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
            airportSuggestion.hierarchyInfo!!.airport!!.airportCode = airport.mAirportCode
            airportSuggestion.hierarchyInfo!!.airport!!.regionId = airport.mRegionId
            airportSuggestion.hierarchyInfo?.isChild = false
            airportSuggestion.hierarchyInfo!!.country = SuggestionV4.Country()
            airportSuggestion.hierarchyInfo?.country?.countryCode = airport.mCountryCode
            airportSuggestion.hierarchyInfo?.country?.name = airport.mCountry
            airportSuggestion.coordinates = SuggestionV4.LatLng()
            return airportSuggestion
        }

        @JvmStatic
        fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }
    }
}
