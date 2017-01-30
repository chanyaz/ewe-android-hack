package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.utils.StrUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.net.URLDecoder
import java.util.Locale

class CustomDeepLinkParser: DeepLinkParser() {

     fun parseCustomDeepLink(data: Uri): DeepLink {
        val routingDestination = data.host.toLowerCase(Locale.US)
        when(routingDestination) {
            "hotelsearch" -> return parseHotelCustomDeepLink(data)
            "flightsearch" -> return parseFlightCustomDeepLink(data)
            "carsearch" -> return parseCarCustomDeepLink(data)
            "activitysearch" -> return parseActivityCustomDeepLink(data)
            "addshareditinerary" -> return parseSharedItineraryCustomDeepLink(data)
            "signin" -> return SignInDeepLink()
            "trips" -> return TripDeepLink()
            "showtrips" -> return TripDeepLink()
            "supportemail" -> return SupportEmailDeepLink()
            "forcebucket" -> return parseForceBucketDeepLink(data)
            else -> return HomeDeepLink()
        }
    }

    private fun parseHotelCustomDeepLink(data: Uri): HotelDeepLink {
        val hotelDeepLink = HotelDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        hotelDeepLink.location = getQueryParameterIfExists(data, queryParameterNames, "location")
        hotelDeepLink.hotelId = getQueryParameterIfExists(data, queryParameterNames, "hotelId")
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
        activityDeepLink.activityID = getQueryParameterIfExists(data, queryParameterNames, "activityID")
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
}