package com.expedia.bookings.deeplink

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.utils.*
import com.mobiata.android.Log
import org.joda.time.format.DateTimeFormat
import java.util.*
import java.util.regex.Pattern

class CustomDeepLinkParser(assets: AssetManager): DeepLinkParser(assets) {

    private val locationId = Pattern.compile("^(ID)?([0-9]+)")

     fun parseCustomDeepLink(data: Uri, context: Context): DeepLink {
        val routingDestination = data.host.toLowerCase(Locale.US)
        when(routingDestination) {
            "hotelsearch" -> return parseHotelCustomDeepLink(data)
            "flightsearch" -> return parseFlightCustomDeepLink(data)
            "carsearch" -> return parseCarCustomDeepLink(data)
            "activitysearch" -> return parseActivityCustomDeepLink(data)
            "addshareditinerary" -> return parseSharedItineraryCustomDeepLink(data)
            "signin" -> return SignInDeepLink()
            "trips" -> return parseTripCustomDeepLink(data)
            "showtrips" -> return TripDeepLink()
            "supportemail" -> return SupportEmailDeepLink()
            "reviewfeedbackemail" -> return ReviewFeedbackEmailDeeplink()
            "forcebucket" -> return parseForceBucketDeepLink(data)
            "packagesearch" -> return parsePackagesSearchCustomDeepLink(data)
            "railsearch" -> return parseRailSearchCustomDeepLink(data)
            "flightshare" -> return parseFlightShareCustomDeepLink(data)
            "replaypackages" -> return parsePackagesReplayCustomDeepLink(data, context)
            else -> return HomeDeepLink()
        }
    }

    private fun parseHotelCustomDeepLink(data: Uri): HotelDeepLink {
        val hotelDeepLink = HotelDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        val location = getQueryParameterIfExists(data, queryParameterNames, "location")
        if (location != null) {
            val matcher = locationId.matcher(location)
            if (matcher.find()) {
                hotelDeepLink.regionId = matcher.group(2)
            }
            else {
                hotelDeepLink.location = location
            }
        }

        hotelDeepLink.hotelId = getQueryParameterIfExists(data, queryParameterNames, "hotelId")
        hotelDeepLink.selectedHotelId = getQueryParameterIfExists(data, queryParameterNames, "selected")
        hotelDeepLink.checkInDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "checkInDate", DateTimeFormat.forPattern("yyyy-MM-dd"))
        hotelDeepLink.checkOutDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "checkOutDate", DateTimeFormat.forPattern("yyyy-MM-dd"))
        hotelDeepLink.sortType = getQueryParameterIfExists(data, queryParameterNames, "sortType")
        hotelDeepLink.numAdults = getIntegerParameterIfExists(data, queryParameterNames, "numAdults")

        if (queryParameterNames.contains("childAges")) {
            hotelDeepLink.children = parseChildAges(data.getQueryParameter("childAges"), hotelDeepLink.numAdults)
        }
        return hotelDeepLink
    }

    private fun parseFlightCustomDeepLink(data: Uri): FlightDeepLink {
        val flightDeepLink = FlightDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        flightDeepLink.origin = getQueryParameterIfExists(data, queryParameterNames, "origin")
        flightDeepLink.destination = getQueryParameterIfExists(data, queryParameterNames, "destination")
        flightDeepLink.departureDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "departureDate", DateTimeFormat.forPattern("yyyy-MM-dd"))
        flightDeepLink.returnDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "returnDate", DateTimeFormat.forPattern("yyyy-MM-dd"))
        flightDeepLink.numAdults = getIntegerParameterIfExists(data, queryParameterNames, "numAdults")

        return flightDeepLink
    }

    private fun parseCarCustomDeepLink(data: Uri): CarDeepLink {
        val carDeepLink = CarDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        carDeepLink.pickupDateTime = getParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "pickupDateTime")
        carDeepLink.dropoffDateTime = getParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "dropoffDateTime")
        carDeepLink.pickupLocation = getQueryParameterIfExists(data, queryParameterNames, "pickupLocation")
        carDeepLink.originDescription = getQueryParameterIfExists(data, queryParameterNames, "originDescription")
        carDeepLink.pickupLocationLat = getQueryParameterIfExists(data, queryParameterNames, "pickupLocationLat")
        carDeepLink.pickupLocationLng = getQueryParameterIfExists(data, queryParameterNames, "pickupLocationLng")

        return carDeepLink
    }

    private fun parseActivityCustomDeepLink(data: Uri): ActivityDeepLink {
        val activityDeepLink = ActivityDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        activityDeepLink.startDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "startDate", DateTimeFormat.forPattern("yyyy-MM-dd"))
        activityDeepLink.location = getQueryParameterIfExists(data, queryParameterNames, "location")
        activityDeepLink.activityID = getQueryParameterIfExists(data, queryParameterNames, "activityId")
        activityDeepLink.filters = getQueryParameterIfExists(data, queryParameterNames, "filters")

        return activityDeepLink
    }

    private fun parseSharedItineraryCustomDeepLink(data: Uri): SharedItineraryDeepLink {
        val sharedItineraryDeepLink = SharedItineraryDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        sharedItineraryDeepLink.url = getQueryParameterIfExists(data, queryParameterNames, "url")

        return sharedItineraryDeepLink
    }

    private fun parseForceBucketDeepLink(data: Uri): ForceBucketDeepLink {
        val forceBucketDeepLink = ForceBucketDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        forceBucketDeepLink.key = getQueryParameterIfExists(data, queryParameterNames, "key")
        forceBucketDeepLink.value = getQueryParameterIfExists(data, queryParameterNames, "value")

        return forceBucketDeepLink
    }

    private fun parsePackagesSearchCustomDeepLink(data: Uri): PackageDeepLink {
        return PackageDeepLink()
    }

    private fun parsePackagesReplayCustomDeepLink(data: Uri, context: Context): PackageDeepLink {
        var packageDeepLink = PackageDeepLink()

        savePackageParamsInSharedPref(data, context)
        return packageDeepLink
    }

    private fun parseRailSearchCustomDeepLink(data: Uri): RailDeepLink {
        return RailDeepLink()
    }

    private fun parseFlightShareCustomDeepLink(data: Uri): FlightShareDeepLink {
        return FlightShareDeepLink()
    }

    private fun parseChildAges(childAgesStr: String, numAdults: Int): List<ChildTraveler>? {
        val childAgesArr = childAgesStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val maxChildren = GuestsPickerUtils.getMaxChildren(numAdults)
        val children = ArrayList<ChildTraveler>()
        try {
            var a = 0
            while (a < childAgesArr.size && children.size < maxChildren) {
                val childAge = Integer.parseInt(childAgesArr[a])

                if (childAge < GuestsPickerUtils.MIN_CHILD_AGE) {
                    Log.w(TAG, "Child age (" + childAge + ") less than that of a child, not adding: "
                            + childAge)
                } else if (childAge > GuestsPickerUtils.MAX_CHILD_AGE) {
                    Log.w(TAG, "Child age ($childAge) not an actual child, ignoring: $childAge")
                } else {
                    children.add(ChildTraveler(childAge, false))
                }
                a++
            }
            if (children.size > 0) {
                Log.d(TAG,
                        "Setting children ages: " + Arrays.toString(children.toTypedArray()))
                return children
            }
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Could not parse childAges: " + childAgesStr, e)
        }
        return null
    }

    private fun parseTripCustomDeepLink(data: Uri): TripDeepLink {
        val tripDeepLink = TripDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        tripDeepLink.itinNum = getQueryParameterIfExists(data, queryParameterNames, "itinNum")

        return tripDeepLink
    }

    private fun savePackageParamsInSharedPref(data: Uri, context: Context) {
        var queryParameterNames = StrUtils.getQueryParameterNames(data)

        val hotelSearchParams = HotelSearchParams()

        hotelSearchParams.origin = getQueryParameterIfExists(data, queryParameterNames, "origin") ?: ""
        hotelSearchParams.destination = getQueryParameterIfExists(data, queryParameterNames, "destination")?: ""
        hotelSearchParams.originID = getQueryParameterIfExists(data, queryParameterNames, "originID")?: ""
        hotelSearchParams.destinationID = getQueryParameterIfExists(data, queryParameterNames, "destinationID")?: ""
        hotelSearchParams.startDate = DeeplinkCreatorUtils.DATE_FORMATTER.parseLocalDate(getQueryParameterIfExists(data, queryParameterNames, "startDate")?: "")
        hotelSearchParams.endDate = DeeplinkCreatorUtils.DATE_FORMATTER.parseLocalDate(getQueryParameterIfExists(data, queryParameterNames, "endDate")?: "")
        hotelSearchParams.originAirportCode = getQueryParameterIfExists(data, queryParameterNames, "originAirportCode")?: ""
        hotelSearchParams.destinationAirportCode = getQueryParameterIfExists(data, queryParameterNames, "destinationAirportCode")?: ""
        hotelSearchParams.noOfTravelers = getQueryParameterIfExists(data, queryParameterNames, "noOfTravelers")?: ""

        DeeplinkSharedPrefParserUtils.saveHotelSearchDeeplinkParams(hotelSearchParams, context)

        val hotelRoomSelectionParams = HotelRoomSelectionParams()
        hotelRoomSelectionParams.selectedRoomTypeCode = getQueryParameterIfExists(data, queryParameterNames, "hotelRoomTypeCode") ?: ""

        DeeplinkSharedPrefParserUtils.saveHotelRoomSelectionParams(hotelRoomSelectionParams, context)

        val hotelSelectionParams = HotelSelectionParams()
        hotelSelectionParams.selectedHotelID = getQueryParameterIfExists(data, queryParameterNames, "hotelID") ?: ""

        DeeplinkSharedPrefParserUtils.saveHotelSelectionParams(hotelSelectionParams, context)

        val inboundCount = (getQueryParameterIfExists(data, queryParameterNames, "inboundCount") ?: "0").toInt()

        val inboundPrefix = "inbound_"

        val flightInboundParamList = getFlightInboundParams(inboundCount, data, queryParameterNames, inboundPrefix)
        DeeplinkSharedPrefParserUtils.saveInboundFlightSelectionParams(flightInboundParamList, context)

        val outboundCount = (getQueryParameterIfExists(data, queryParameterNames, "outboundCount") ?: "0").toInt()

        val outboundPrefix = "outbound_"

        val flightOutboundParamList = getFlightOutboundParams(outboundCount, data, queryParameterNames, outboundPrefix)

        DeeplinkSharedPrefParserUtils.saveOutboundFlightSelectionParams(flightOutboundParamList, context)

    }

    private fun getFlightInboundParams(inboundCount: Int, data: Uri, queryParameterNames: MutableSet<String>, prefix: String): ArrayList<FlightInboundParams> {
        val flightParamList = ArrayList<FlightInboundParams>()

        for (i in 1..inboundCount) {
            val flightInboundParams = FlightInboundParams()
            val index = i - 1
            flightInboundParams.airlineCode = getQueryParameterIfExists(data, queryParameterNames, prefix + "airlineCode_" + index) ?: ""
            flightInboundParams.flightNumber = getQueryParameterIfExists(data, queryParameterNames, prefix + "flight_number_" + index) ?: ""
            flightParamList.add(flightInboundParams)
        }
        return flightParamList
    }

    private fun getFlightOutboundParams(inboundCount: Int, data: Uri, queryParameterNames: MutableSet<String>, prefix: String): ArrayList<FlightOutboundParams> {
        val flightParamList = ArrayList<FlightOutboundParams>()

        for (i in 1..inboundCount) {
            val flightInboundParams = FlightOutboundParams()
            val index = i -1
            flightInboundParams.airlineCode = getQueryParameterIfExists(data, queryParameterNames, prefix + "airlineCode_" + index) ?: ""
            flightInboundParams.flightNumber = getQueryParameterIfExists(data, queryParameterNames, prefix + "flight_number_" + index) ?: ""
            flightParamList.add(flightInboundParams)
        }
        return flightParamList
    }

}
