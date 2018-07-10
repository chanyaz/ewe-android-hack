package com.mobiata.mocke3

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import java.util.concurrent.TimeUnit

// Mocks out various mobile Expedia APIs
class ExpediaDispatcher(protected var fileOpener: FileOpener, dispatcherSettings: Map<DispatcherSettingsKeys, String>) : Dispatcher() {

    constructor(fileOpener: FileOpener) : this(fileOpener, emptyMap())

    private var lastSignInEmail: String = ""
    private val travelAdRequests = hashMapOf<String, Int>()
    private val hotelRequestDispatcher = HotelRequestDispatcher(fileOpener)
    private val flightApiRequestDispatcher = FlightApiRequestDispatcher(fileOpener)
    private val lxApiRequestDispatcher = LxApiRequestDispatcher(fileOpener)
    private val multiItemApiRequestDispatcher = MultiItemApiRequestDispatcher(fileOpener)
    private val railApiRequestDispatcher = RailApiRequestDispatcher(fileOpener)
    private val satelliteServiceRequestDispatcher = SatelliteApiRequestDispatcher(fileOpener)
    private val cardFeeServiceRequestDispatcher = CardFeeServiceRequestDispatcher(fileOpener)
    private val sosApiRequestDispatcher = SOSApiRequestDispatcher(fileOpener)
    private val osApiRequestDispatcher = OSApiRequestDispatcher(fileOpener)
    private val travelGraphRequestDispatcher = TravelGraphApiRequestDispatcher(fileOpener)
    private val flightMApiRequestDispatcher = FlightMApiRequestDispatcher(fileOpener)
    private val hotelShortlistRequestDispatcher = HotelShortlistApiRequestDispatcher(fileOpener)
    private val hotelReviewsRequestDispatcher = HotelReviewsApiRequestDispatcher(fileOpener)
    private val tripsDispatcher = TripsDispatcher(fileOpener, dispatcherSettings)

    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (!doesRequestHaveValidUserAgent(request)) {
            throw UnsupportedOperationException("Valid user-agent not passed. I expect to see a user-agent resembling: ExpediaBookings/x.x.x (EHad; Mobiata)" + request)
        }

        if (request.path.startsWith("/m/api/config/feature")) {
            return satelliteServiceRequestDispatcher.dispatch(request)
        }

        // Card fee API
        if (request.path.startsWith("/api/flight/trip/cardFee")) {
            return cardFeeServiceRequestDispatcher.dispatch(request)
        }

        //Holiday Calendar API
        if (request.path.startsWith("/m/api/calendar")) {
            return dispatchHolidayInfo()
        }

        // Rails API
        if (RailApiRequestMatcher.isRailApiRequest(request.path)) {
            return railApiRequestDispatcher.dispatch(request)
        }

        // TravelGraph API
        if (TravelGraphApiRequestMatcher.isTravelGraphRequest(request.path)) {
            return travelGraphRequestDispatcher.dispatch(request)
        }

        // Hotel Shortlist API
        if (HotelShortlistApiRequestMatcher.isHotelShortlistRequest(request.path)) {
            return hotelShortlistRequestDispatcher.dispatch(request)
        }

        // MID API
        if (request.path.startsWith("/api/multiitem/v1")) {
            return multiItemApiRequestDispatcher.dispatch(request)
        }

        // Hotels API
        if (request.path.startsWith("/m/api/hotel") || request.path.startsWith("/api/m/trip/coupon") || request.path.startsWith("/api/m/trip/remove/coupon")) {
            return hotelRequestDispatcher.dispatch(request)
        }

        //Flight Baggage Info API
        if (request.path.contains("/api/flight/baggagefees")) {
            var params = request.body.readUtf8()
            return dispatchBaggageInfo(params.contains("4"))
        }

        // Flights MAPI
        if (request.path.contains("/m/api/flight")) {
            return flightMApiRequestDispatcher.dispatch(request)
        }

        // Flights API
        if (request.path.contains("/api/flight")) {
            return flightApiRequestDispatcher.dispatch(request)
        }

        // LX API
        if (request.path.contains("/lx/api") || request.path.contains("m/api/lx")) {
            return lxApiRequestDispatcher.dispatch(request)
        }

        //SOS Member Only Deals
        if (request.path.contains("sos/offers/member-only-deals")) {
            return sosApiRequestDispatcher.dispatch(request)
        }

        //OS Last Minute Deals
        if (request.path.contains("/offers/v2/getOffers")) {
            return osApiRequestDispatcher.dispatch(request)
        }

        // AbacusV2 API
        if (request.path.contains("/api/bucketing/v1/evaluateExperiments")) {
            val params = parseHttpRequest(request)
            val tpid = params["tpid"] ?: return make404()

            return makeResponse("/api/bucketing/happy$tpid.json")
        }

        // AbacusV2 API
        if (request.path.contains("/api/bucketing/v1/logExperiments")) {
            return makeEmptyResponse()
        }

        // TODO - move Trips into own dispatcher

        // Trips API
        if (request.path.startsWith("/api/trips?")) {
            return dispatchTrip(request)
        }

        // Trips Details API
        if (request.path.startsWith("/api/trips/")) {
            return dispatchTripDetails(request)
        }

        // Calculate points API
        if (request.path.contains("/api/trip/calculatePoints")) {
            return dispatchCalculatePoints(request)
        }

        // Expedia Suggest
        if (request.path.startsWith("/hint/es") || request.path.startsWith("/api/v4")) {
            return dispatchSuggest(request)
        }

        // GAIA Suggest
        if (request.path.contains("/features")) {
            return dispatchGaiaSuggest(request)
        }

        // User API
        if (request.path.contains("/api/user/sign-in")) {
            return dispatchSignIn(request)
        }

        // Insurance API
        if (request.path.startsWith("/m/api/insurance")) {
            return dispatchInsurance(request)
        }

        // Omniture
        if (request.path.startsWith("/b/ss")) {
            return makeEmptyResponse()
        }

        // Static content like Mobiata image server
        if (request.path.startsWith("/static")) {
            return dispatchStaticContent(request)
        }

        // User Profile/Stored Traveler info
        if (request.path.startsWith("/api/user/profile")) {
            return dispatchUserProfile(request)
        }

        // Travel Ad Impression
        if (request.path.startsWith("/TravelAdsService/v3/Hotels/TravelAdImpression")) {
            return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdImpression")
        }

        // Travel Ad Click
        if (request.path.startsWith("/TravelAdsService/v3/Hotels/TravelAdClick")) {
            return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdClick")
        }

        // Travel Ad Beacon
        if (request.path.startsWith("/travel")) {
            return dispatchTravelAd("/travel")
        }

        // Travel Ad on Confirmation
        if (request.path.startsWith("/ads/hooklogic")) {
            return dispatchTravelAd("/ads/hooklogic")
        }

        //Hotel Reviews Summary
        if (HotelReviewsApiRequestMatcher.isHotelReviewsRequest(request.path)) {
            return hotelReviewsRequestDispatcher.dispatch(request)
        }

        //TNS User API
        if (request.path.contains("m/api/register/user")) {
            return dispatchTNSResponse()
        }

        if (request.path.contains("/m/api/deregister")) {
            return dispatchTNSDeregistrationResponse()
        }

        //Trip folders
        if (request.path.contains("/m/api/trips/tripfolders")) {
            return tripsDispatcher.dispatch(request)
        }
        if (request.path.contains("/m/api/notification/received")) {
            return dispatchTNSNotificationReceivedResponse()
        }

        return make404()
    }

    private fun doesRequestHaveValidUserAgent(request: RecordedRequest): Boolean {
        val userAgent = request.headers.get("user-agent")
        val regExp = Regex("^[a-zA-Z]+/\\d+\\.\\d+(\\.\\d+)?(.*) \\(EHad; Mobiata\\)$")
        return regExp.matches(userAgent.toString())
    }

    /////////////////////////////////////////////////////////////////////////////
    // Path dispatching
    private fun dispatchTripDetails(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        val startIndex = request.path.lastIndexOf("/") + 1
        var endIndex = request.path.indexOfFirst { it.toString() == "?" }
        if (endIndex == -1) {
            endIndex = request.path.length
        }

        val fileName = request.path.substring(startIndex, endIndex)

        val pacificTimeZone = DateTimeZone.forID("America/Los_Angeles")
        val startOfTodayPacific = DateTime.now().withZone(pacificTimeZone).withTimeAtStartOfDay()

        val hotelCheckIn = startOfTodayPacific.plusDays(10).withHourOfDay(11).withMinuteOfHour(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).withHourOfDay(18).withMinuteOfHour(4)
        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.millis / 1000)
        params.put("hotelCheckInTzOffset", "" + pacificTimeZone.getOffset(hotelCheckIn.millis) / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.millis / 1000)
        params.put("hotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(hotelCheckOut.millis) / 1000)
        params.put("offerExpiresTimeRaw", DateTime().plusDays(2).toString())

        var responseCode = 200
        when (fileName) {
            "error_trip_response" -> responseCode = 403
            "error_bad_request_trip_response" -> responseCode = 400
        }

        return makeResponse("/api/trips/$fileName.json", params, responseCode)
    }

    private fun dispatchTrip(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        if (lastSignInEmail.isNotEmpty() && lastSignInEmail == "trip_error@mobiata.com") {
            return makeResponse("/api/trips/error_trip_response.json", params)
        }

        // Common to all trips
        // NOTE: using static hour offset so that daylight savings doesn't muck with the data

        val pacificTimeZone = DateTimeZone.forID("America/Los_Angeles")
        val startOfTodayPacific = DateTime.now().withZone(pacificTimeZone).withTimeAtStartOfDay()

        val hotelCheckIn = startOfTodayPacific.plusDays(10).withHourOfDay(11).withMinuteOfHour(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).withHourOfDay(18).withMinuteOfHour(4)

        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.millis / 1000)
        params.put("hotelCheckInTzOffset", "" + pacificTimeZone.getOffset(hotelCheckIn.millis) / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.millis / 1000)
        params.put("hotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(hotelCheckOut.millis) / 1000)

        return makeResponse("/api/trips/happy.json", params)
    }

    private fun dispatchSuggest(request: RecordedRequest): MockResponse {
        var type: String? = ""
        var latlong: String? = ""
        var lob: String? = ""
        val params = parseHttpRequest(request)
        if (params.containsKey("type")) {
            type = params["type"]
        }
        if (params.containsKey("latlong")) {
            latlong = params["latlong"]
        }
        if (params.containsKey("lob")) {
            lob = params["lob"]
        }

        when {
            request.path.startsWith("/hint/es/v2/ac/en_US") -> {
                val requestPath = request.path
                val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'))
                return makeResponse("hint/es/v2/ac/en_US/" + unUrlEscape(filename) + ".json")
            }
            request.path.startsWith("/hint/es/v3/ac/en_US") -> return if (type == "14") {
                makeResponse("/hint/es/v3/ac/en_US/suggestion_city.json")
            } else {
                makeResponse("/hint/es/v3/ac/en_US/suggestion.json")
            }
            request.path.startsWith("/hint/es/v1/nearby/en_US") -> return when {
                latlong == "31.32|75.57" -> makeResponse("/hint/es/v1/nearby/en_US/suggestion_with_no_lx_activities.json")
                type == "14" -> makeResponse("/hint/es/v1/nearby/en_US/suggestion_city.json")
                else -> makeResponse("/hint/es/v1/nearby/en_US/suggestion.json")
            }

        // City
            request.path.startsWith("/api/v4/typeahead/") -> when (lob) {
                "FLIGHTS" -> {
                    //Material Flights
                    if (request.path.startsWith("/api/v4/typeahead/lon?")) {
                        return makeResponse("/api/v4/suggestion_flights_lon.json")
                    }
                    return makeResponse("/api/v4/suggestion_flights.json")
                }
                "PACKAGES" -> {
                    if (request.path.startsWith("/api/v4/typeahead/del?")) {
                        return makeResponse("/api/v4/suggestion_packages_del.json")
                    } else if (request.path.startsWith("/api/v4/typeahead/sfo?")) {
                        return makeResponse("/api/v4/suggestion_sfo.json")
                    } else
                        return makeResponse("/api/v4/suggestion.json")
                }
                else -> {
                    val requestPath = request.path
                    val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?')).toLowerCase()
                    return if ("Flights".equals(lob, false)) {
                        makeResponse("/api/v4/suggestion_" + unUrlEscape(filename) + ".json")
                    } else {
                        makeResponse("/api/v4/suggestion.json")
                    }
                }
            }
        }

        return make404()
    }

    private fun dispatchBaggageInfo(isOutBound: Boolean): MockResponse {
        if (isOutBound) {
            return makeResponse("api/flight/baggageFeeInfoOutbound.json")
        } else {
            return makeResponse("api/flight/baggageFeeInfoInbound.json")
        }
    }

    private fun dispatchHolidayInfo(): MockResponse {
        return makeResponse("api/flight/holidayCalendar.json")
    }

    private fun dispatchTNSResponse(): MockResponse {
        return makeResponse("api/trips/tns_registration_user_response.json")
    }

    private fun dispatchTNSDeregistrationResponse(): MockResponse {
        return makeResponse("api/trips/tns_registration_user_response.json")
    }

    private fun dispatchTNSNotificationReceivedResponse(): MockResponse {
        return makeResponse("api/trips/tns_notification_received_response.json")
    }

    private fun dispatchGaiaSuggest(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        var latitude: String? = ""
        var longitude: String? = ""
        var lob: String? = ""
        var locale: String? = ""

        if (params.containsKey("lat")) {
            latitude = params["lat"]
        }
        if (params.containsKey("lng")) {
            longitude = params["lng"]
        }
        if (params.containsKey("lob")) {
            lob = params["lob"]
        }
        if (params.containsKey("locale")) {
            locale = params["locale"]
        }
        if ((latitude == "31.32") && (longitude == "75.57")) {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_no_lx_activities.json")
        }
        if ((latitude == "3.0") && (longitude == "3.0") && (lob == "hotels")) {
            return makeResponse("/api/gaia/nearby_gaia_suggestion.json")
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "hotels") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result.json")
        }
        if ((latitude == "0.0") && (longitude == "0.0") && lob == "hotels") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_zero_results.json")
        }
        if ((latitude == "3.0") && (longitude == "3.0") && lob == "lx") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_lx.json")
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "lx" && locale == "fr_FR") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result_lx_french.json")
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "lx" && locale == "en_US") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result_lx_english.json")
        }
        if ((latitude == "0.0") && (longitude == "0.0") && lob == "lx") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_zero_results.json")
        }
        return make404()
    }

    private fun dispatchSignIn(request: RecordedRequest): MockResponse {
        // TODO Handle the case when there's no email parameter in 2nd sign-in request
        val params = parseHttpRequest(request)
        lastSignInEmail = params["email"] ?: lastSignInEmail
        params.put("email", lastSignInEmail)
        return if (lastSignInEmail.isNotEmpty()) {
            makeResponse("api/user/sign-in/$lastSignInEmail.json", params)
        } else {
            makeResponse("api/user/sign-in/qa-ehcc@mobiata.com.json", params)
        }
    }

    private fun dispatchStaticContent(request: RecordedRequest): MockResponse =
            makeResponse(request.path)

    private fun dispatchUserProfile(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        return makeResponse("api/user/profile/user_profile_" + params["tuid"] + ".json")
    }

    private fun makeResponse(fileName: String, params: Map<String, String>? = null, responseCode: Int = 200): MockResponse =
            makeResponse(fileName, params, fileOpener, responseCode)

    private fun dispatchTravelAd(endPoint: String): MockResponse {
        val count = travelAdRequests[endPoint] ?: 0
        travelAdRequests.put(endPoint, count + 1)
        return makeEmptyResponse()
    }

    private fun dispatchCalculatePoints(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        val tripParams = params["tripId"]?.split("|") ?: listOf(params["tripId"])
        val response = makeResponse("/m/api/trip/calculatePoints/" + tripParams[0] + ".json")
        if (tripParams.size > 1) {
            response.setBodyDelay(tripParams[1]?.toLong() ?: 0, TimeUnit.MILLISECONDS)
        }
        return response
    }

    private fun dispatchInsurance(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)

        val tripId = params["tripId"]!!
        val baseTripId = tripId.substring(0, tripId.indexOf("_with_insurance"))
        params.put("productKey", baseTripId)

        val filename = if (params["insuranceProductId"].isNullOrEmpty())
            "${baseTripId}_with_insurance_available"
        else
            "${baseTripId}_with_insurance_selected"

        return makeResponse("api/flight/trip/create/$filename.json", params)
    }

    fun numOfTravelAdRequests(key: String): Int = travelAdRequests[key] ?: 0
}

