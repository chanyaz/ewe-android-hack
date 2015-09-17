package com.expedia.bookings.utils

import com.expedia.bookings.data
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
                    return gson.fromJson(hotelSearchParamsJSON, javaClass<HotelSearchParams>())
                } catch (jse: JsonSyntaxException) {
                    throw UnsupportedOperationException()
                }

            }
            return null
        }

        public fun getHotelV2SearchParams(params: data.HotelSearchParams): HotelSearchParams {
            val suggestionV4 = SuggestionV4()
            suggestionV4.gaiaId = params.getRegionId()
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
            val v2params = HotelSearchParams(suggestionV4, params.getCheckInDate(), params.getCheckOutDate(), params.getNumAdults(), childList)
            return v2params
        }


        public fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(javaClass<LocalDate>(), LocalDateTypeAdapter(PATTERN)).create()
        }


    }

}
