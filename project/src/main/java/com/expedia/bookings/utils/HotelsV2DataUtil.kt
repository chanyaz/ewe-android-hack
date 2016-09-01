package com.expedia.bookings.utils

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelSearchParams.SearchType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.ArrayList

class HotelsV2DataUtil {

    companion object {
        fun getHotelV2SearchParamsFromJSON(hotelSearchParamsJSON: String): HotelSearchParams? {
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

        fun getHotelV2SearchParams(params: com.expedia.bookings.data.HotelSearchParams): HotelSearchParams {
            val suggestionV4 = SuggestionV4()
            suggestionV4.hotelId = params.hotelId
            suggestionV4.gaiaId = params.getRegionId()
            suggestionV4.coordinates = SuggestionV4.LatLng()
            suggestionV4.coordinates.lat = params.searchLatitude
            suggestionV4.coordinates.lng = params.searchLongitude

            suggestionV4.type = params.searchType.name
            val regionNames = SuggestionV4.RegionNames()
            regionNames.displayName = params.getQuery()
            regionNames.shortName = params.getQuery()
            suggestionV4.regionNames = regionNames
            val childTraveler = params.getChildren()
            val childList = ArrayList<Int>()
            if (childTraveler != null && !childTraveler.isEmpty()) {
                for (index in 0..childTraveler.size - 1) {
                    childList.add(childTraveler.get(index).getAge())
                }
            }
            val hasValidDates = JodaUtils.isBeforeOrEquals(LocalDate.now(), params.getCheckInDate())
            val checkInDate = if (hasValidDates) params.getCheckInDate() else LocalDate.now()
            val checkOutDate = if (hasValidDates) params.getCheckOutDate() else LocalDate.now().plusDays(1)
            val filterUnavailable = !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSearchScreenSoldOutTest);
            val v2params = HotelSearchParams(suggestionV4, checkInDate, checkOutDate, params.getNumAdults(), childList, Db.getUser()?.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false, filterUnavailable)
            return v2params
        }


        fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }

        fun getHotelV2ParamsFromFlightV2Params(flightLegs: MutableList<FlightLeg>?, flightSearchParams: FlightSearchParams): HotelSearchParams {
            val outboundLeg = flightLegs?.first()
            val lastOutboundSegment = outboundLeg?.segments?.get(outboundLeg.segments.size - 1)

            val localCheckInDate = LocalDate(DateTime.parse(lastOutboundSegment?.arrivalTimeRaw))
            var localCheckoutDate = localCheckInDate.plusDays(1)
            if (flightLegs?.size == 2) {
                val inboundLeg = flightLegs?.last()
                localCheckoutDate = LocalDate(DateTime.parse(inboundLeg?.segments?.get(0)?.departureTimeRaw))
            }

            val numFlightTravelers = flightSearchParams.guests
            val listOfChildTravelerAges = flightSearchParams.children
            val numAdultTravelers = numFlightTravelers - listOfChildTravelerAges.size
            var numAdultsPerHotelRoom = Math.min(GuestsPickerUtils.getMaxAdults(0), numAdultTravelers)
            numAdultsPerHotelRoom = Math.max(numAdultsPerHotelRoom, GuestsPickerUtils.MIN_ADULTS) // just in case default...

            val suggestionV4 = SuggestionV4()
            suggestionV4.gaiaId = flightSearchParams.destination?.gaiaId
            suggestionV4.coordinates = flightSearchParams.destination?.coordinates
            suggestionV4.type = SearchType.CITY.name
            suggestionV4.regionNames = flightSearchParams.destination?.regionNames

            val filterUnavailable = !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSearchScreenSoldOutTest)

            return HotelSearchParams(suggestionV4, localCheckInDate, localCheckoutDate, numAdultsPerHotelRoom, listOfChildTravelerAges, false, filterUnavailable)
        }
    }
}
