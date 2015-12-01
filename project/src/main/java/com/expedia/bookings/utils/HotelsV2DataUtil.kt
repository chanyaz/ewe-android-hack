package com.expedia.bookings.utils

import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.joda.time.LocalDate
import java.util.ArrayList

public class HotelsV2DataUtil {

    companion object {
        public fun getHotelV2SearchParamsFromJSON(hotelSearchParamsJSON: String): HotelSearchParams? {
            val gson = generateGson()

            if (Strings.isNotEmpty(hotelSearchParamsJSON) ) {
                try {
                    return gson.fromJson(hotelSearchParamsJSON, HotelSearchParams::class.java)
                } catch (jse: JsonSyntaxException) {
                    throw UnsupportedOperationException()
                }

            }
            return null
        }

        public fun getHotelV2SearchParams(params: com.expedia.bookings.data.HotelSearchParams): HotelSearchParams {
            val suggestionV4 = SuggestionV4()
            suggestionV4.hotelId = params.hotelId
            suggestionV4.gaiaId = params.getRegionId()
            suggestionV4.coordinates = SuggestionV4.LatLng()
            suggestionV4.coordinates.lat = params.searchLatitude
            suggestionV4.coordinates.lng = params.searchLongitude

            suggestionV4.type = params.searchType.name()
            val regionNames = SuggestionV4.RegionNames()
            regionNames.displayName = params.getQuery()
            regionNames.shortName = params.getQuery()
            suggestionV4.regionNames = regionNames
            val childTraveler = params.getChildren()
            val childList = ArrayList<Int>()
            if (childTraveler != null && !childTraveler.isEmpty()) {
                for (index in 0..childTraveler.size() - 1) {
                    childList.add(childTraveler.get(index).getAge())
                }
            }
            val hasValidDates = JodaUtils.isBeforeOrEquals(LocalDate.now(), params.getCheckInDate())
            val checkInDate = if (hasValidDates) params.getCheckInDate() else LocalDate.now()
            val checkOutDate = if (hasValidDates) params.getCheckOutDate() else LocalDate.now().plusDays(1)
            val v2params = HotelSearchParams(suggestionV4, checkInDate, checkOutDate, params.getNumAdults(), childList)
            return v2params
        }


        public fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }


    }

}
