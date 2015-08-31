package com.mobiata.mocke3

import com.google.gson.JsonParser
import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.Calendar
import java.util.Date

// Mocks out various mobile Expedia APIs
public class ExpediaDispatcher(protected var fileOpener: FileOpener) : Dispatcher() {

    private val travelAdRequests = hashMapOf<String, Int>()
    private val hotelRequestDispatcher = HotelRequestDispatcher(fileOpener)
    private val flightApiRequestDispatcher = FlightApiRequestDispatcher(fileOpener)
    private val carApiRequestDispatcher = CarApiRequestDispatcher(fileOpener)
    private val lxApiRequestDispatcher = LxApiRequestDispatcher(fileOpener)

    @throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {

        // Hotels API
        if (request.getPath().startsWith("/m/api/hotel") || request.getPath().startsWith("/api/m/trip/coupon")) {
            return hotelRequestDispatcher.dispatch(request)
        }

        // Flights API
        if (request.getPath().contains("/api/flight")) {
            return flightApiRequestDispatcher.dispatch(request)
        }

        // Cars API
        if (request.getPath().contains("/m/api/cars")) {
            return carApiRequestDispatcher.dispatch(request)
        }

        // LX API
        if (request.getPath().contains("/lx/api") || request.getPath().contains("m/api/lx")) {
            return lxApiRequestDispatcher.dispatch(request)
        }

        // AbacusV2 API
        if (request.getPath().contains("/api/bucketing/v1/evaluateExperiments")) {
            return makeResponse("/api/bucketing/happy.json")
        }

        // AbacusV2 API
        if (request.getPath().contains("/api/bucketing/v1/logExperiments")) {
            return makeEmptyResponse()
        }

        // Trips API
        if (request.getPath().startsWith("/api/trips")) {
            return dispatchTrip(request)
        }

        // Expedia Suggest
        if (request.getPath().startsWith("/hint/es") || request.getPath().startsWith("/api/v4") ) {
            return dispatchSuggest(request)
        }

        // User API
        if (request.getPath().contains("/api/user/sign-in")) {
            return dispatchSignIn(request)
        }

        // Omniture
        if (request.getPath().startsWith("/b/ss")) {
            return makeEmptyResponse()
        }

        // Static content like Mobiata image server
        if (request.getPath().startsWith("/static")) {
            return dispatchStaticContent(request)
        }

        // User Profile/Stored Traveler info
        if (request.getPath().startsWith("/api/user/profile")) {
            return dispatchUserProfile(request)
        }

        // Travel Ad Impression
        if (request.getPath().startsWith("/TravelAdsService/v3/Hotels/TravelAdImpression")) {
            return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdImpression")
        }

        // Travel Ad Click
        if (request.getPath().startsWith("/TravelAdsService/v3/Hotels/TravelAdClick")) {
            return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdClick")
        }

        // Travel Ad Beacon
        if (request.getPath().startsWith("/travel")) {
            return dispatchTravelAd("/travel")
        }

        // Travel Ad on Confirmation
        if (request.getPath().startsWith("/ads/hooklogic")) {
            return dispatchTravelAd("/ads/hooklogic")
        }

        return make404()
    }

    /////////////////////////////////////////////////////////////////////////////
    // Path dispatching

    private fun dispatchTrip(request: RecordedRequest): MockResponse {
        val params = parseRequest(request)

        // Common to all trips
        val startOfTodayPacific = DateTime.now().withTimeAtStartOfDay().withZone(DateTimeZone.forOffsetHours(-7))
        val startOfTodayEastern = DateTime.now().withTimeAtStartOfDay().withZone(DateTimeZone.forOffsetHours(-4))
        val pacificDaylightTzOffset = -7 * 60 * 60.toLong()
        val easternDaylightTzOffset = -4 * 60 * 60.toLong()
        params.put("tzOffsetPacific", "" + pacificDaylightTzOffset)
        params.put("tzOffsetEastern", "" + easternDaylightTzOffset)

        // Inject hotel DateTimes
        val hotelCheckIn = startOfTodayPacific.plusDays(10).plusHours(11).plusMinutes(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).plusHours(18).plusMinutes(4)
        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.getMillis() / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.getMillis() / 1000)

        // Inject flight DateTimes
        val outboundFlightDeparture = startOfTodayPacific.plusDays(14).plusHours(11).plusMinutes(32)
        val outboundFlightArrival = startOfTodayEastern.plusDays(14).plusHours(18).plusMinutes(4)
        val inboundFlightDeparture = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(59)
        val inboundFlightArrival = startOfTodayPacific.plusDays(22).plusHours(22).plusMinutes(11)
        params.put("outboundFlightDepartureEpochSeconds", "" + outboundFlightDeparture.getMillis() / 1000)
        params.put("outboundFlightArrivalEpochSeconds", "" + outboundFlightArrival.getMillis() / 1000)
        params.put("inboundFlightDepartureEpochSeconds", "" + inboundFlightDeparture.getMillis() / 1000)
        params.put("inboundFlightArrivalEpochSeconds", "" + inboundFlightArrival.getMillis() / 1000)

        // Inject air attach times
        params.put("airAttachOfferExpiresEpochSeconds", "" + startOfTodayPacific.plusDays(1).getMillis() / 1000);

        // Inject car DateTimes
        val carPickup = startOfTodayEastern.plusDays(14).plusHours(11).plusMinutes(32)
        val carDropoff = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(29)
        params.put("carPickupEpochSeconds", "" + carPickup.getMillis() / 1000)
        params.put("carDropoffEpochSeconds", "" + carDropoff.getMillis() / 1000)

        // Inject lx DateTimes
        val lxStart = startOfTodayPacific.plusDays(25).plusHours(11)
        val lxEnd = startOfTodayPacific.plusDays(25).plusHours(17)
        params.put("lxStartEpochSeconds", "" + lxStart.getMillis() / 1000)
        params.put("lxEndEpochSeconds", "" + lxEnd.getMillis() / 1000)


        // Inject package DateTimes
        val pckgStart = startOfTodayPacific.plusDays(35).plusHours(4)
        val pckgEnd = startOfTodayPacific.plusDays(41).plusHours(12)
        val pckgHotelCheckIn = startOfTodayPacific.plusDays(35).plusHours(8).plusMinutes(0)
        val pckgHotelCheckOut = startOfTodayPacific.plusDays(40).plusHours(2).plusMinutes(0)
        val pckgOutboundFlightDeparture = startOfTodayPacific.plusDays(35).plusHours(4)
        val pckgOutboundFlightArrival = startOfTodayEastern.plusDays(35).plusHours(6).plusMinutes(4)
        val pckgInboundFlightDeparture = startOfTodayEastern.plusDays(40).plusHours(10)
        val pckgInboundFlightArrival = startOfTodayPacific.plusDays(40).plusHours(12)
        params.put("pckgStartEpochSeconds", "" + pckgStart.getMillis() / 1000)
        params.put("pckgEndEpochSeconds", "" + pckgEnd.getMillis() / 1000)
        params.put("pckgOutboundFlightDepartureEpochSeconds", "" + pckgOutboundFlightDeparture.getMillis() / 1000)
        params.put("pckgOutboundFlightArrivalEpochSeconds", "" + pckgOutboundFlightArrival.getMillis() / 1000)
        params.put("pckgInboundFlightDepartureEpochSeconds", "" + pckgInboundFlightDeparture.getMillis() / 1000)
        params.put("pckgInboundFlightArrivalEpochSeconds", "" + pckgInboundFlightArrival.getMillis() / 1000)
        params.put("pckgHotelCheckInEpochSeconds", "" + pckgHotelCheckIn.getMillis() / 1000)
        params.put("pckgHotelCheckOutEpochSeconds", "" + pckgHotelCheckOut.getMillis() / 1000)


        return makeResponse("/api/trips/happy.json", params)
    }

    private fun dispatchSuggest(request: RecordedRequest): MockResponse {
        var type: String? = ""
        var latlong: String? = ""
        val params = parseRequest(request)
        if (params.containsKey("type")) {
            type = params.get("type")
        }
        if (params.containsKey("latlong")) {
            latlong = params.get("latlong")
        }

        if (request.getPath().startsWith("/hint/es/v2/ac/en_US")) {
            val requestPath = request.getPath()
            val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'))
            return makeResponse("hint/es/v2/ac/en_US/" + unUrlEscape(filename) + ".json")
        } else if (request.getPath().startsWith("/hint/es/v3/ac/en_US")) {
            if (type == "14") {
                return makeResponse("/hint/es/v3/ac/en_US/suggestion_city.json")
            } else {
                return makeResponse("/hint/es/v3/ac/en_US/suggestion.json")
            }
        } else if (request.getPath().startsWith("/hint/es/v1/nearby/en_US")) {
            if (latlong == "31.32|75.57") {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion_with_no_lx_activities.json")
            } else if (type == "14") {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion_city.json")
            } else {
                return makeResponse("/hint/es/v1/nearby/en_US/suggestion.json")
            }// City
        } else if (request.getPath().startsWith("/api/v4/typeahead/")) {
            return makeResponse("/api/v4/suggestion.json")
        }
        return make404()
    }

    private fun dispatchSignIn(request: RecordedRequest): MockResponse {
        // TODO Handle the case when there's no email parameter in 2nd sign-in request
        val params = parseRequest(request)
        params.put("email", "qa-ehcc@mobiata.com")
        return makeResponse("api/user/sign-in/login.json", params)
    }

    private fun dispatchStaticContent(request: RecordedRequest): MockResponse {
        return makeResponse(request.getPath())
    }

    private fun dispatchUserProfile(request: RecordedRequest): MockResponse {
        val params = parseRequest(request)
        return makeResponse("api/user/profile/user_profile_" + params.get("tuid") + ".json")
    }

    private fun makeResponse(fileName: String, params: Map<String, String>? = null): MockResponse {
        return makeResponse(fileName, params, fileOpener)
    }

    private fun dispatchTravelAd(endPoint: String): MockResponse {
        var count = 0;
        if (travelAdRequests.get(endPoint) != null) {
            count = travelAdRequests.get(endPoint);
        }
        travelAdRequests.put(endPoint, count + 1)
        return makeEmptyResponse();
    }

    public fun numOfTravelAdRequests(key: String): Int {
        if (travelAdRequests.get(key) != null) {
            return travelAdRequests.get(key)
        }
        return 0
    }
}

