package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import java.util.concurrent.TimeUnit
import kotlin.collections.listOf
import kotlin.text.Regex
import kotlin.text.contains

// Mocks out various mobile Expedia APIs
class ExpediaDispatcher(protected var fileOpener: FileOpener) : Dispatcher() {

    private var lastSignInEmail: String = ""
    private val travelAdRequests = hashMapOf<String, Int>()
    private val hotelRequestDispatcher = HotelRequestDispatcher(fileOpener)
    private val flightApiRequestDispatcher = FlightApiRequestDispatcher(fileOpener)
    private val carApiRequestDispatcher = CarApiRequestDispatcher(fileOpener)
    private val lxApiRequestDispatcher = LxApiRequestDispatcher(fileOpener)
    private val packagesApiRequestDispatcher = PackagesApiRequestDispatcher(fileOpener)
    private val railApiRequestDispatcher = RailApiRequestDispatcher(fileOpener)

    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!doesRequestHaveValidUserAgent(request)) {
            throw UnsupportedOperationException("Valid user-agent not passed. I expect to see a user-agent resembling: ExpediaBookings/x.x.x (EHad; Mobiata)")
        }

        // Rails API
        if (request.path.startsWith("/rails/ecom/v1/shopping")) {
            return railApiRequestDispatcher.dispatch(request)
        }

        // Packages API
        if (request.path.startsWith("/getpackages/v1") || request.path.startsWith("/api/packages")) {
            return packagesApiRequestDispatcher.dispatch(request)
        }
        
        // Hotels API
        if (request.path.startsWith("/m/api/hotel") || request.path.startsWith("/api/m/trip/coupon") || request.path.startsWith("/api/m/trip/remove/coupon")) {
            return hotelRequestDispatcher.dispatch(request)
        }

        // Flights API
        if (request.path.contains("/api/flight")) {
            return flightApiRequestDispatcher.dispatch(request)
        }

        // Cars API
        if (request.path.contains("/m/api/cars")) {
            return carApiRequestDispatcher.dispatch(request)
        }

        // LX API
        if (request.path.contains("/lx/api") || request.path.contains("m/api/lx")) {
            return lxApiRequestDispatcher.dispatch(request)
        }

        // AbacusV2 API
        if (request.path.contains("/api/bucketing/v1/evaluateExperiments")) {
            return makeResponse("/api/bucketing/happy.json")
        }

        // AbacusV2 API
        if (request.path.contains("/api/bucketing/v1/logExperiments")) {
            return makeEmptyResponse()
        }

        // Trips API
        if (request.path.startsWith("/api/trips")) {
            return dispatchTrip(request)
        }

        // Calculate points API
        if (request.path.contains("/api/trip/calculatePoints")) {
            return dispatchCalculatePoints(request)
        }

        // Expedia Suggest
        if (request.path.startsWith("/hint/es") || request.path.startsWith("/api/v4") ) {
            return dispatchSuggest(request)
        }

        // User API
        if (request.path.contains("/api/user/sign-in")) {
            return dispatchSignIn(request)
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

        // Hotel Reviews
        if (request.path.startsWith("/api/hotelreviews")) {
            return dispatchReviews()
        }

        return make404()
    }

    fun doesRequestHaveValidUserAgent(request: RecordedRequest): Boolean {
        val userAgent = request.headers.get("user-agent")
        val regExp = Regex("^ExpediaBookings\\/[0-9]\\.[0-9](\\.[0-9]){0,1}(.*) \\(EHad; Mobiata\\)$")
        return regExp.matches(userAgent.toString())
    }

    /////////////////////////////////////////////////////////////////////////////
    // Path dispatching

    private fun dispatchTrip(request: RecordedRequest): MockResponse {
        val params = parseRequest(request)

        // Common to all trips
        // NOTE: using static hour offset so that daylight savings doesn't muck with the data
        val pacificTimeZone = DateTimeZone.forID("America/Los_Angeles")
        val easternTimeZone = DateTimeZone.forID("America/New_York")
        val startOfTodayPacific = DateTime.now().withZone(pacificTimeZone).withTimeAtStartOfDay()
        val startOfTodayEastern = DateTime.now().withZone(easternTimeZone).withTimeAtStartOfDay()

        // Inject hotel DateTimes
        val hotelCheckIn = startOfTodayPacific.plusDays(10).plusHours(11).plusMinutes(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).plusHours(18).plusMinutes(4)
        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.millis / 1000)
        params.put("hotelCheckInTzOffset", "" + pacificTimeZone.getOffset(hotelCheckIn.millis) / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.millis / 1000)
        params.put("hotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(hotelCheckOut.millis) / 1000)

        // Inject flight DateTimes
        val outboundFlightDeparture = startOfTodayPacific.plusDays(14).plusHours(11).plusMinutes(32)
        val outboundFlightArrival = startOfTodayEastern.plusDays(14).plusHours(18).plusMinutes(4)
        val inboundFlightDeparture = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(59)
        val inboundFlightArrival = startOfTodayPacific.plusDays(22).plusHours(22).plusMinutes(11)
        params.put("outboundFlightDepartureEpochSeconds", "" + outboundFlightDeparture.millis / 1000)
        params.put("outboundFlightDepartureTzOffset", "" + pacificTimeZone.getOffset(outboundFlightDeparture.millis) / 1000)
        params.put("outboundFlightArrivalEpochSeconds", "" + outboundFlightArrival.millis / 1000)
        params.put("outboundFlightArrivalTzOffset", "" + easternTimeZone.getOffset(outboundFlightArrival.millis) / 1000)
        params.put("inboundFlightDepartureEpochSeconds", "" + inboundFlightDeparture.millis / 1000)
        params.put("inboundFlightDepartureTzOffset", "" + easternTimeZone.getOffset(inboundFlightDeparture.millis) / 1000)
        params.put("inboundFlightArrivalEpochSeconds", "" + inboundFlightArrival.millis / 1000)
        params.put("inboundFlightArrivalTzOffset", "" + pacificTimeZone.getOffset(inboundFlightArrival.millis) / 1000)

        // Inject air attach times
        var airAttachExpiry = startOfTodayPacific.plusDays(1);
        // near midnight, during standard time, this can get stretched to 2 days (25 hours), but test expects 1 day
        // NOTE: this is all because we force the timezones in the test to daylight savings time, even during standard time periods
        while (Days.daysBetween(DateTime.now().toLocalDate(), airAttachExpiry.toLocalDate()).days > 1) {
            airAttachExpiry = airAttachExpiry.minusHours(1)
        }
        params.put("airAttachOfferExpiresEpochSeconds", "" + airAttachExpiry.millis / 1000);
        params.put("airAttachOfferExpiresTzOffset", "" + pacificTimeZone.getOffset(airAttachExpiry.millis) / 1000);

        // Inject car DateTimes
        val carPickup = startOfTodayEastern.plusDays(14).plusHours(11).plusMinutes(32)
        val carDropoff = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(29)
        params.put("carPickupEpochSeconds", "" + carPickup.millis / 1000)
        params.put("carPickupTzOffset", "" + easternTimeZone.getOffset(carPickup.millis) / 1000)
        params.put("carDropoffEpochSeconds", "" + carDropoff.millis / 1000)
        params.put("carDropoffTzOffset", "" + easternTimeZone.getOffset(carDropoff.millis) / 1000)

        // Inject lx DateTimes
        val lxStart = startOfTodayPacific.plusDays(25).plusHours(11)
        val lxEnd = startOfTodayPacific.plusDays(25).plusHours(17)
        params.put("lxStartEpochSeconds", "" + lxStart.millis / 1000)
        params.put("lxStartTzOffset", "" + pacificTimeZone.getOffset(lxStart.millis) / 1000)
        params.put("lxEndEpochSeconds", "" + lxEnd.millis / 1000)
        params.put("lxEndTzOffset", "" + pacificTimeZone.getOffset(lxEnd.millis) / 1000)


        // Inject package DateTimes
        val pckgStart = startOfTodayPacific.plusDays(35).plusHours(4)
        val pckgEnd = startOfTodayPacific.plusDays(41).plusHours(12)
        val pckgHotelCheckIn = startOfTodayPacific.plusDays(35).plusHours(8).plusMinutes(0)
        val pckgHotelCheckOut = startOfTodayPacific.plusDays(40).plusHours(2).plusMinutes(0)
        val pckgOutboundFlightDeparture = startOfTodayPacific.plusDays(35).plusHours(4)
        val pckgOutboundFlightArrival = startOfTodayPacific.plusDays(35).plusHours(6).plusMinutes(4)
        val pckgInboundFlightDeparture = startOfTodayPacific.plusDays(40).plusHours(10)
        val pckgInboundFlightArrival = startOfTodayPacific.plusDays(40).plusHours(12)
        params.put("pckgStartEpochSeconds", "" + pckgStart.millis / 1000)
        params.put("pckgStartTzOffset", "" + pacificTimeZone.getOffset(pckgStart.millis) / 1000)
        params.put("pckgEndEpochSeconds", "" + pckgEnd.millis / 1000)
        params.put("pckgEndTzOffset", "" + pacificTimeZone.getOffset(pckgEnd.millis) / 1000)
        params.put("pckgOutboundFlightDepartureEpochSeconds", "" + pckgOutboundFlightDeparture.millis / 1000)
        params.put("pckgOutboundFlightDepartureTzOffset", "" + pacificTimeZone.getOffset(pckgOutboundFlightDeparture.millis) / 1000)
        params.put("pckgOutboundFlightArrivalEpochSeconds", "" + pckgOutboundFlightArrival.millis / 1000)
        params.put("pckgOutboundFlightArrivalTzOffset", "" + pacificTimeZone.getOffset(pckgOutboundFlightArrival.millis) / 1000)
        params.put("pckgInboundFlightDepartureEpochSeconds", "" + pckgInboundFlightDeparture.millis / 1000)
        params.put("pckgInboundFlightDepartureTzOffset", "" + pacificTimeZone.getOffset(pckgInboundFlightDeparture.millis) / 1000)
        params.put("pckgInboundFlightArrivalEpochSeconds", "" + pckgInboundFlightArrival.millis / 1000)
        params.put("pckgInboundFlightArrivalTzOffset", "" + pacificTimeZone.getOffset(pckgInboundFlightArrival.millis) / 1000)
        params.put("pckgHotelCheckInEpochSeconds", "" + pckgHotelCheckIn.millis / 1000)
        params.put("pckgHotelCheckInTzOffset", "" + pacificTimeZone.getOffset(pckgHotelCheckIn.millis) / 1000)
        params.put("pckgHotelCheckOutEpochSeconds", "" + pckgHotelCheckOut.millis / 1000)
        params.put("pckgHotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(pckgHotelCheckOut.millis) / 1000)


        return makeResponse("/api/trips/happy.json", params)
    }

    private fun dispatchSuggest(request: RecordedRequest): MockResponse {
        var type: String? = ""
        var latlong: String? = ""
        var lob: String? = ""
        val params = parseRequest(request)
        if (params.containsKey("type")) {
            type = params.get("type")
        }
        if (params.containsKey("latlong")) {
            latlong = params.get("latlong")
        }
        if (params.containsKey("lob")) {
            lob = params.get("lob")
        }

        if (request.path.startsWith("/hint/es/v2/ac/en_US")) {
            val requestPath = request.path
            val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'))
            return makeResponse("hint/es/v2/ac/en_US/" + unUrlEscape(filename) + ".json")
        } else if (request.path.startsWith("/hint/es/v3/ac/en_US")) {
            if (type == "14") {
                return makeResponse("/hint/es/v3/ac/en_US/suggestion_city.json")
            } else {
                return makeResponse("/hint/es/v3/ac/en_US/suggestion.json")
            }
        } else if (request.path.startsWith("/hint/es/v1/nearby/en_US")) {
            if (latlong == "31.32|75.57") {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion_with_no_lx_activities.json")
            } else if (type == "14") {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion_city.json")
            } else {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion.json")
            }// City
        } else if (request.path.startsWith("/api/v4/typeahead/")) {
            if (lob == "Flights") {
                val requestPath = request.path
                val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'))
                return makeResponse("/api/v4/suggestion_" + unUrlEscape(filename) + ".json")
            }
            else {
                return makeResponse("/api/v4/suggestion.json")
            }
        } else if (request.path.startsWith("/api/v4/nearby/")) {
            if (latlong == "31.32|75.57") {
                return makeResponse("/api/v4/suggestion_with_no_lx_activities.json")
            }
            return makeResponse("/api/v4/suggestion_nearby.json")
        }
        return make404()
    }

    private fun dispatchSignIn(request: RecordedRequest): MockResponse {
        // TODO Handle the case when there's no email parameter in 2nd sign-in request
        val params = parseRequest(request)
        lastSignInEmail = params.get("email") ?: lastSignInEmail
        params.put("email", lastSignInEmail)
        return if (lastSignInEmail.isNotEmpty()) {
            makeResponse("api/user/sign-in/" + lastSignInEmail + ".json", params)
        } else {
            makeResponse("api/user/sign-in/qa-ehcc@mobiata.com.json", params)
        }
    }

    private fun dispatchStaticContent(request: RecordedRequest): MockResponse {
        return makeResponse(request.path)
    }

    private fun dispatchUserProfile(request: RecordedRequest): MockResponse {
        val params = parseRequest(request)
        return makeResponse("api/user/profile/user_profile_" + params.get("tuid") + ".json")
    }

    private fun makeResponse(fileName: String, params: Map<String, String>? = null): MockResponse {
        return makeResponse(fileName, params, fileOpener)
    }

    private fun dispatchTravelAd(endPoint: String): MockResponse {
        val count = travelAdRequests.get(endPoint) ?: 0
        travelAdRequests.put(endPoint, count + 1)
        return makeEmptyResponse();
    }

    private fun dispatchReviews(): MockResponse {
        return makeResponse("api/hotelreviews/hotel/happy.json")
    }

    private fun dispatchCalculatePoints(request: RecordedRequest): MockResponse {
        val params = parseRequest(request)
        val tripParams = params["tripId"]?.split("|") ?: listOf(params["tripId"])
        val response = makeResponse("/m/api/trip/calculatePoints/"+ tripParams[0] +".json")
        if (tripParams.size > 1) {
            response.setBodyDelay(tripParams[1]?.toLong() ?: 0  , TimeUnit.MILLISECONDS)
        }
        return response
    }

    fun numOfTravelAdRequests(key: String): Int {
        return travelAdRequests.get(key) ?: 0
    }
}

