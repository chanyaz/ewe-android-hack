package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelSearchParams.SearchType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.expedia.util.LoyaltyUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.squareup.phrase.Phrase
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

        fun getHotelV2SearchParams(context: Context, params: com.expedia.bookings.data.HotelSearchParams): HotelSearchParams {
            val suggestionV4 = SuggestionV4()
            suggestionV4.hotelId = params.hotelId
            suggestionV4.gaiaId = params.regionId
            suggestionV4.coordinates = SuggestionV4.LatLng()
            suggestionV4.coordinates.lat = params.searchLatitude
            suggestionV4.coordinates.lng = params.searchLongitude

            suggestionV4.type = params.searchType.name
            val regionNames = SuggestionV4.RegionNames()
            regionNames.displayName = params.query
            regionNames.shortName = params.query
            suggestionV4.regionNames = regionNames
            val childTraveler = params.children
            val childList = ArrayList<Int>()
            if (childTraveler != null && !childTraveler.isEmpty()) {
                for (index in 0..childTraveler.size - 1) {
                    childList.add(childTraveler[index].age)
                }
            }
            val hasValidDates = JodaUtils.isBeforeOrEquals(LocalDate.now(), params.checkInDate)
            val checkInDate = if (hasValidDates) params.checkInDate else LocalDate.now()
            val checkOutDate = if (hasValidDates) params.checkOutDate else LocalDate.now().plusDays(1)
            val shopWithPointsAvailable = LoyaltyUtil.isShopWithPointsAvailable(Ui.getApplication(context).appComponent().userStateManager())
            val v2params = HotelSearchParams(suggestionV4, checkInDate, checkOutDate, params.numAdults, childList, shopWithPointsAvailable, true, params.sortType, params.mctc)

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

            return HotelSearchParams(suggestionV4, localCheckInDate, localCheckoutDate, numAdultsPerHotelRoom, listOfChildTravelerAges, shopWithPoints = true, filterUnavailable = true)
        }

        fun getHotelRatingContentDescription(context: Context, hotelStarRating: Int): String {
            if (hotelStarRating <= 0) return ""
            var contDesc: String
            if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) {
                var stringID: Int = 0
                when (hotelStarRating) {
                    1 -> stringID = R.string.star_circle_rating_one_cont_desc
                    2 -> stringID = R.string.star_circle_rating_two_cont_desc
                    3 -> stringID = R.string.star_circle_rating_three_cont_desc
                    4 -> stringID = R.string.star_circle_rating_four_cont_desc
                    5 -> stringID = R.string.star_circle_rating_five_cont_desc
                }
                contDesc = context.getString(stringID)

            } else {
                contDesc = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_star_rating_cont_desc_TEMPLATE, hotelStarRating))
                        .put("rating", hotelStarRating)
                        .format()
                        .toString()
            }
            return contDesc
        }
    }
}
