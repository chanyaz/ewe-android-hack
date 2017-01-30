package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.StrUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.net.URLDecoder
import java.util.Locale
import java.util.regex.Pattern

class UniversalDeepLinkParser: DeepLinkParser() {

    private val LEG_PATTERN = Pattern.compile("from:(.+),to:(.+),departure:(.+)")
    private val AIRPORT_CODE = Pattern.compile("[^A-Z]?([A-Z]{3})[^A-Z]?")
    private val DATETIME = Pattern.compile("([^T]+)T?")
    private val NUM_ADULTS = Pattern.compile("adults:([0-9])+,")
    private val TIME = Pattern.compile("([0-9]{1,2})([0-9]{2})(AM|PM)")

     fun parseUniversalDeepLink(data: Uri): DeepLink {
        var routingDestination: String = ""

        if (data.path.contains("m/trips/shared")) {
            routingDestination = "shareditin"
        }
        else if (data.host.equals(ProductFlavorFeatureConfiguration.getInstance().hostnameForShortUrl)) {
            routingDestination = "shorturl"
        }
        else if (data.path.contains("mobile/deeplink")) {
             routingDestination = data.path.substring(data.path.indexOf("mobile/deeplink") + "mobile/deeplink".length)
                     .toLowerCase(Locale.US)
        }

        when(routingDestination) {
            "/hotel-search" -> return parseHotelUniversalDeepLink(data)
            "/flights-search" -> return parseFlightUniversalDeepLink(data)
            "/carsearch" -> return parseCarUniversalDeepLink(data)
            "/things-to-do/search" -> return parseActivityUniversalDeepLink(data)
            "shareditin" -> return parseSharedItineraryUniversalDeepLink(data)
            "shorturl" -> return parseShortUrlDeepLink(data)
            "/user/signin" -> return SignInDeepLink()
            "/trips" -> return TripDeepLink()
            else -> return HomeDeepLink()
        }
    }

    private fun parseHotelUniversalDeepLink(data: Uri): HotelDeepLink {
        val hotelDeepLink = HotelDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        hotelDeepLink.checkInDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "startDate", DateTimeFormat.forPattern("MM/dd/yyyy"))
        hotelDeepLink.checkOutDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "endDate", DateTimeFormat.forPattern("MM/dd/yyyy"))
        hotelDeepLink.numAdults = getIntegerParameterIfExists(data, queryParameterNames, "adults")
        hotelDeepLink.sortType = getQueryParameterIfExists(data, queryParameterNames, "sort")
        if (queryParameterNames.contains("regionId")) {
            hotelDeepLink.location = StringBuilder("ID").append(data.getQueryParameter("regionId")).toString()
        }

        return hotelDeepLink
    }

    private fun parseFlightUniversalDeepLink(data: Uri): DeepLink {
        val flightDeepLink = FlightDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        if (queryParameterNames.contains("leg1")) {
            val leg1 = data.getQueryParameter("leg1")
            val leg1Matcher = LEG_PATTERN.matcher(leg1)

            if (leg1Matcher.find()) {
                val origin = leg1Matcher.group(1)
                val destination = leg1Matcher.group(2)
                val departure = leg1Matcher.group(3)

                val airportCodeOriginMatcher = AIRPORT_CODE.matcher(origin)
                val airportCodeDestinationMatcher = AIRPORT_CODE.matcher(destination)
                val departureMatcher = DATETIME.matcher(departure)

                if (airportCodeOriginMatcher.find() && airportCodeDestinationMatcher.find() && departureMatcher.find()) {
                    flightDeepLink.origin = airportCodeOriginMatcher.group(1)
                    flightDeepLink.destination = airportCodeDestinationMatcher.group(1)
                    try {
                        val departureDateStr = departureMatcher.group(1)
                        flightDeepLink.departureDate = LocalDate.parse(URLDecoder.decode(departureDateStr, "UTF-8"), DateTimeFormat.forPattern("MM/dd/yyyy"))
                    }
                    catch (e: Exception) {
                    }
                }
            }
        }

        if (queryParameterNames.contains("leg2")) {
            val leg2 = data.getQueryParameter("leg2")
            val leg2Matcher = LEG_PATTERN.matcher(leg2)

            if (leg2Matcher.find()) {
                val returnDate = leg2Matcher.group(3)
                val returnDateMatcher = DATETIME.matcher(returnDate)

                if (returnDateMatcher.find()) {
                    try {
                        val returnDateStr = returnDateMatcher.group(1)
                        flightDeepLink.returnDate = LocalDate.parse(URLDecoder.decode(returnDateStr, "UTF-8"), DateTimeFormat.forPattern("MM/dd/yyyy"))
                    }
                    catch (e: Exception) {
                    }
                }
            }
        }

        if (queryParameterNames.contains("passengers")) {
            val passengers = data.getQueryParameter("passengers")
            val numAdultsMatcher = NUM_ADULTS.matcher(passengers)
            if (numAdultsMatcher.find()) {
                flightDeepLink.numAdults = Integer.parseInt(numAdultsMatcher.group(1))
            }
        }

        return flightDeepLink
    }

    private fun parseCarUniversalDeepLink(data: Uri): CarDeepLink {
        val carDeepLink = CarDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        carDeepLink.pickupDateTime = getCarParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "date1", "time1", DateTimeFormat.forPattern("MM/dd/yyyy"))
        carDeepLink.dropoffDateTime = getCarParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "date2", "time2", DateTimeFormat.forPattern("MM/dd/yyyy"))

        if (queryParameterNames.contains("locn")) {
            val airportCodeMatcher = AIRPORT_CODE.matcher(data.getQueryParameter("locn"))
            if (airportCodeMatcher.find()) {
                carDeepLink.pickupLocation = airportCodeMatcher.group(1)
            }
        }

        return carDeepLink
    }

    private fun getCarParsedDateTimeQueryParameterIfExists(data: Uri, queryParameterNames: Set<String>, dateParamName: String, timeParamName: String, dateTimeFormatter: DateTimeFormatter): DateTime? {

        if (queryParameterNames.contains(dateParamName) && queryParameterNames.contains(timeParamName)) {
            try {
                var dateStr = data.getQueryParameter(dateParamName)
                var date = LocalDate.parse(URLDecoder.decode(dateStr, "UTF-8"), dateTimeFormatter)

                val timeMatcher = TIME.matcher(data.getQueryParameter(timeParamName))
                if (timeMatcher.find()) {
                    var hour = Integer.parseInt(timeMatcher.group(1))
                    val min = Integer.parseInt(timeMatcher.group(2))
                    val meridiem = timeMatcher.group(3)
                    if (meridiem.equals("PM")) {
                        hour += 12
                    }
                    return DateTime(date.year, date.monthOfYear, date.dayOfMonth, hour, min)
                }
            }
            catch (e: Exception) {
            }
        }
        return null
    }

    private fun parseActivityUniversalDeepLink(data: Uri): ActivityDeepLink {
        val activityDeepLink = ActivityDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        activityDeepLink.startDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "startDate", DateTimeFormat.forPattern("MM/dd/yyyy"))
        activityDeepLink.filters = getQueryParameterIfExists(data, queryParameterNames, "categories")

        if (queryParameterNames.contains("location")) {
            activityDeepLink.location = StrUtils.formatCityName(data.getQueryParameter("location"))
        }

        return activityDeepLink
    }

    private fun parseSharedItineraryUniversalDeepLink(data: Uri): SharedItineraryDeepLink {
        val sharedItineraryDeepLink = SharedItineraryDeepLink()
        sharedItineraryDeepLink.url = data.toString()
        return sharedItineraryDeepLink
    }

    private fun parseShortUrlDeepLink(data: Uri): ShortUrlDeepLink {
        val shortUrlDeepLink = ShortUrlDeepLink()
        shortUrlDeepLink.shortUrl = data.toString()
        return shortUrlDeepLink
    }
}
