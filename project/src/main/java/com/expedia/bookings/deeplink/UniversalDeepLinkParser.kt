package com.expedia.bookings.deeplink

import android.content.res.AssetManager
import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.data.pos.PointOfSaleConfigHelper
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.GuestsPickerUtils
import com.expedia.bookings.utils.StrUtils
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.json.JSONException
import org.json.JSONObject
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale
import java.util.regex.Pattern

class UniversalDeepLinkParser(assets: AssetManager): DeepLinkParser(assets){

    private val LEG_PATTERN = Pattern.compile("from:(.+),to:(.+),departure:(.+)")
    private val AIRPORT_CODE = Pattern.compile("[^A-Z]?([A-Z]{3})[^A-Z]?")
    private val DATETIME = Pattern.compile("([^T]+)T?")
    private val NUM_ADULTS = Pattern.compile("adults:([0-9]+),")
    private val TIME = Pattern.compile("([0-9]{1,2})([0-9]{2})(AM|PM)")
    private val HOTEL_INFO_SITE = Pattern.compile("/[^\\.]+\\.h(\\d+)\\.hotel-information")
    private val TRIPS_ITIN_NUM = Pattern.compile("/trips/([0-9]+)")
    private val SIGN_IN = Pattern.compile(".+(?=\\/signin/?$).+")

     fun parseUniversalDeepLink(data: Uri): DeepLink {
         var routingDestination = getRoutingDestination(data)
         val dateFormat = getDateFormatForPOS(data)

        when(routingDestination) {
            "/hotel-search" -> return parseHotelUniversalDeepLink(data, dateFormat)
            "/hotels" -> return parseHotelUniversalDeepLink(data, dateFormat)
            "hotel-infosite" -> return parseHotelInfoSiteUniversalDeepLink(data, dateFormat)
            "/flights-search" -> return parseFlightUniversalDeepLink(data, dateFormat)
            "/carsearch" -> return parseCarUniversalDeepLink(data, dateFormat)
            "/things-to-do/search" -> return parseActivityUniversalDeepLink(data, dateFormat)
            "shareditin" -> return parseSharedItineraryUniversalDeepLink(data)
            "shorturl" -> return parseShortUrlDeepLink(data)
            "/signin" -> return SignInDeepLink()
            "/member-pricing" -> return MemberPricingDeepLink()
            "/trips" -> return parseTripUniversalDeepLink(data)
            else ->
                return HomeDeepLink()
        }
    }

    private fun getRoutingDestination(data: Uri): String {
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
            if (HOTEL_INFO_SITE.matcher(routingDestination).find()) {
                routingDestination = "hotel-infosite"
            } else if (TRIPS_ITIN_NUM.matcher(routingDestination).find()) {
                routingDestination = "/trips"
            } else if (SIGN_IN.matcher(routingDestination).find()) {
                routingDestination = "/signin"
            }
        }

        if (routingDestination.endsWith('/')) {
            routingDestination = routingDestination.trimEnd('/')
        }

        return routingDestination
    }

    private fun parseHotelInfoSiteUniversalDeepLink(data: Uri, dateFormat: String): HotelDeepLink {
        val hotelDeepLink = HotelDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)
        var matcher = HOTEL_INFO_SITE.matcher(data.path.toLowerCase())
        if (matcher.find()) {
            hotelDeepLink.hotelId = matcher.group(1)
        }
        hotelDeepLink.checkInDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "chkin", DateTimeFormat.forPattern(dateFormat))
        hotelDeepLink.checkOutDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "chkout", DateTimeFormat.forPattern(dateFormat))
        hotelDeepLink.mctc = getNullableIntegerParameterIfExists(data, queryParameterNames, "mctc")
        if (queryParameterNames.contains("rm1")) {
            val passengers = data.getQueryParameter("rm1").split(":".toRegex(), 2)
            if (passengers.size > 0) {
                hotelDeepLink.numAdults = Integer.parseInt(passengers[0].substring(1, passengers[0].length))
                if (passengers.size > 1) {
                    hotelDeepLink.children = parseChildAges(passengers[1], hotelDeepLink.numAdults)
                }
            }
        }

        return hotelDeepLink
    }

    private fun parseHotelUniversalDeepLink(data: Uri, dateFormat: String): HotelDeepLink {
        val hotelDeepLink = HotelDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        hotelDeepLink.checkInDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "startDate", DateTimeFormat.forPattern(dateFormat))
        hotelDeepLink.checkOutDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "endDate", DateTimeFormat.forPattern(dateFormat))
        hotelDeepLink.numAdults = getIntegerParameterIfExists(data, queryParameterNames, "adults")
        hotelDeepLink.sortType = getQueryParameterIfExists(data, queryParameterNames, "sort")
        hotelDeepLink.regionId = getQueryParameterIfExists(data, queryParameterNames, "regionId")
        hotelDeepLink.mctc = getNullableIntegerParameterIfExists(data, queryParameterNames, "mctc")

        if (data.toString().toLowerCase().contains("/hotels") && queryParameterNames.size == 1 && hotelDeepLink.sortType?.toLowerCase() == "discounts") {
            hotelDeepLink.memberOnlyDealSearch = true
        }

        return hotelDeepLink
    }

    private fun parseFlightUniversalDeepLink(data: Uri, dateFormat: String): DeepLink {
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
                        flightDeepLink.departureDate = LocalDate.parse(URLDecoder.decode(departureDateStr, "UTF-8"), DateTimeFormat.forPattern(dateFormat))
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
                        flightDeepLink.returnDate = LocalDate.parse(URLDecoder.decode(returnDateStr, "UTF-8"), DateTimeFormat.forPattern(dateFormat))
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

    private fun parseCarUniversalDeepLink(data: Uri, dateFormat: String): CarDeepLink {
        val carDeepLink = CarDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        carDeepLink.pickupDateTime = getCarParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "date1", "time1", DateTimeFormat.forPattern(dateFormat))
        carDeepLink.dropoffDateTime = getCarParsedDateTimeQueryParameterIfExists(data, queryParameterNames, "date2", "time2", DateTimeFormat.forPattern(dateFormat))

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

    private fun parseActivityUniversalDeepLink(data: Uri, dateFormat: String): ActivityDeepLink {
        val activityDeepLink = ActivityDeepLink()
        val queryParameterNames = StrUtils.getQueryParameterNames(data)

        activityDeepLink.startDate = getParsedLocalDateQueryParameterIfExists(data, queryParameterNames, "startDate", DateTimeFormat.forPattern(dateFormat))
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

    private fun parseChildAges(childAgesStr: String, numAdults: Int): List<ChildTraveler>? {
        val childAgesArr = childAgesStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val maxChildren = GuestsPickerUtils.getMaxChildren(numAdults)
        val children = ArrayList<ChildTraveler>()
        try {
            var a = 0
            while (a < childAgesArr.size && children.size < maxChildren) {
                val childAge = Integer.parseInt(childAgesArr[a].substring(1, childAgesArr[a].length))
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

    private fun getDateFormatForPOS(uri: Uri) : String {
        val defaultDateFormat = "MM/dd/yyyy"

        if (ASSETS != null) {
            val configHelper = PointOfSaleConfigHelper(ASSETS, ProductFlavorFeatureConfiguration.getInstance().posConfigurationPath)
            val stream = configHelper.openPointOfSaleConfiguration()

            try {
                val jsonData = IoUtils.convertStreamToString(stream)
                val posData = JSONObject(jsonData)
                val keys = posData.keys()

                keys.forEach { key ->
                    val posJson = posData.getJSONObject(key)
                    if (posJson.getString("url") == uri.host.replace("www.", "")) {
                        return posJson.getString("deepLinkDateFormat")
                    }
                }
                return defaultDateFormat
            }
            catch (e: JSONException) {
                return defaultDateFormat
            }
            finally {
                stream.close()
            }
        }
        else {
            return defaultDateFormat
        }
    }

    private fun parseTripUniversalDeepLink(data: Uri): TripDeepLink {
        val tripDeepLink = TripDeepLink()
        val matcher = TRIPS_ITIN_NUM.matcher(data.path.toLowerCase())
        if (matcher.find()) {
            tripDeepLink.itinNum = matcher.group(1)
        }
        return tripDeepLink
    }
}
