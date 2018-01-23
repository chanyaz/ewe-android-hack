package com.expedia.bookings.deeplink

import android.content.res.AssetManager
import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.utils.GuestsPickerUtils
import com.expedia.bookings.utils.StrUtils
import com.mobiata.android.Log
import org.joda.time.format.DateTimeFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale
import java.util.regex.Pattern

class CustomDeepLinkParser(assets: AssetManager) : DeepLinkParser(assets) {

    private val locationId = Pattern.compile("^(ID)?([0-9]+)")

     fun parseCustomDeepLink(data: Uri): DeepLink {
        val routingDestination = data.host.toLowerCase(Locale.US)
        when (routingDestination) {
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
            } else {
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
}
