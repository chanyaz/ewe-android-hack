package com.mobiata.mocke3

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import java.util.concurrent.TimeUnit

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
    private val feedsApiRequestDispatcher = FeedsApiRequestDispatcher(fileOpener)
    private val cardFeeServiceRequestDispatcher = CardFeeServiceRequestDispatcher(fileOpener)

    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {

        if (!doesRequestHaveValidUserAgent(request)) {
            throw UnsupportedOperationException("Valid user-agent not passed. I expect to see a user-agent resembling: ExpediaBookings/x.x.x (EHad; Mobiata)")
        }

        // Card fee API
        if (request.path.startsWith("/api/flight/trip/cardFee")) {
            return cardFeeServiceRequestDispatcher.dispatch(request)
        }

        //Feeds API
        if(request.path.startsWith("/feeds/api")){
            return feedsApiRequestDispatcher.dispatch(request)
        }

        // Rails API
        if (request.path.startsWith("/rails") || request.path.startsWith("/shop") || request.path.startsWith("/trip") ||
                request.path.startsWith("/domain/trip") || request.path.startsWith("/domain/static")) {
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
            val params = parseHttpRequest(request)
            val tpid = params["tpid"] ?: return make404()

            return makeResponse("/api/bucketing/happy$tpid.json")
        }

        // AbacusV2 API
        if (request.path.contains("/api/bucketing/v1/logExperiments")) {
            return makeEmptyResponse()
        }

        // TODO - move Trips into own dispatcher

        // Trips API - room upgrade
        if (request.path.matches(Regex("^/api/trips.*/upgradeOffers\\?.*$"))) {
            return dispatchUpgradeOffersResponse()
        }

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
        if (request.path.startsWith("/hint/es") || request.path.startsWith("/api/v4") ) {
            return dispatchSuggest(request)
        }

        //GAIA Suggest
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
    private fun dispatchTripDetails(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        val startIndex = request.path.lastIndexOf("/") + 1
        var endIndex = request.path.indexOfFirst { it.toString() == "?" }
        if (endIndex == -1) {
            endIndex = request.path.length
        }

        val fileName = request.path.substring(startIndex, endIndex)

        val pacificTimeZone = DateTimeZone.forID("America/Los_Angeles")
        val easternTimeZone = DateTimeZone.forID("America/New_York")
        val startOfTodayPacific = DateTime.now().withZone(pacificTimeZone).withTimeAtStartOfDay()

        val hotelCheckIn = startOfTodayPacific.plusDays(10).withHourOfDay(11).withMinuteOfHour(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).withHourOfDay(18).withMinuteOfHour(4)
        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.millis / 1000)
        params.put("hotelCheckInTzOffset", "" + pacificTimeZone.getOffset(hotelCheckIn.millis) / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.millis / 1000)
        params.put("hotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(hotelCheckOut.millis) / 1000)

        var responseCode = 200
        when(fileName) {
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
        val easternTimeZone = DateTimeZone.forID("America/New_York")
        val startOfTodayPacific = DateTime.now().withZone(pacificTimeZone).withTimeAtStartOfDay()
        val startOfTodayEastern = DateTime.now().withZone(easternTimeZone).withTimeAtStartOfDay()

        // Inject hotel DateTimes
        val hotelCheckIn = startOfTodayPacific.plusDays(10).withHourOfDay(11).withMinuteOfHour(32)
        val hotelCheckOut = startOfTodayPacific.plusDays(12).withHourOfDay(18).withMinuteOfHour(4)
        params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.millis / 1000)
        params.put("hotelCheckInTzOffset", "" + pacificTimeZone.getOffset(hotelCheckIn.millis) / 1000)
        params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.millis / 1000)
        params.put("hotelCheckOutTzOffset", "" + pacificTimeZone.getOffset(hotelCheckOut.millis) / 1000)

        // Inject flight DateTimes
        val outboundFlightDeparture = startOfTodayPacific.plusDays(14).withHourOfDay(11).withMinuteOfHour(32)
        val outboundFlightArrival = startOfTodayEastern.plusDays(14).withHourOfDay(18).withMinuteOfHour(4)
        val inboundFlightDeparture = startOfTodayEastern.plusDays(22).withHourOfDay(18).withMinuteOfHour(59)
        val inboundFlightArrival = startOfTodayPacific.plusDays(22).withHourOfDay(22).withMinuteOfHour(11)
        params.put("outboundFlightDepartureEpochSeconds", "" + outboundFlightDeparture.millis / 1000)
        params.put("outboundFlightDepartureTzOffset", "" + pacificTimeZone.getOffset(outboundFlightDeparture.millis) / 1000)
        params.put("outboundFlightArrivalEpochSeconds", "" + outboundFlightArrival.millis / 1000)
        params.put("outboundFlightArrivalTzOffset", "" + easternTimeZone.getOffset(outboundFlightArrival.millis) / 1000)
        params.put("inboundFlightDepartureEpochSeconds", "" + inboundFlightDeparture.millis / 1000)
        params.put("inboundFlightDepartureTzOffset", "" + easternTimeZone.getOffset(inboundFlightDeparture.millis) / 1000)
        params.put("inboundFlightArrivalEpochSeconds", "" + inboundFlightArrival.millis / 1000)
        params.put("inboundFlightArrivalTzOffset", "" + pacificTimeZone.getOffset(inboundFlightArrival.millis) / 1000)

        // Inject air attach times
        var airAttachExpiry = startOfTodayPacific.plusDays(1)
        // near midnight, during standard time, this can get stretched to 2 days (25 hours), but test expects 1 day
        // NOTE: this is all because we force the timezones in the test to daylight savings time, even during standard time periods
        while (Days.daysBetween(DateTime.now().toLocalDate(), airAttachExpiry.toLocalDate()).days > 1) {
            airAttachExpiry = airAttachExpiry.minusHours(1)
        }

        while (Days.daysBetween(DateTime.now().toLocalDate(), airAttachExpiry.toLocalDate()).days < 1) {
            airAttachExpiry = airAttachExpiry.plusHours(1)
        }

        params.put("airAttachOfferExpiresEpochSeconds", "" + airAttachExpiry.millis / 1000);
        params.put("airAttachOfferExpiresTzOffset", "" + pacificTimeZone.getOffset(airAttachExpiry.millis) / 1000);

        // Inject car DateTimes
        val carPickup = startOfTodayEastern.plusDays(14).withHourOfDay(11).withMinuteOfHour(32)
        val carDropoff = startOfTodayEastern.plusDays(22).withHourOfDay(18).withMinuteOfHour(29)
        params.put("carPickupEpochSeconds", "" + carPickup.millis / 1000)
        params.put("carPickupTzOffset", "" + easternTimeZone.getOffset(carPickup.millis) / 1000)
        params.put("carDropoffEpochSeconds", "" + carDropoff.millis / 1000)
        params.put("carDropoffTzOffset", "" + easternTimeZone.getOffset(carDropoff.millis) / 1000)

        // Inject lx DateTimes
        val lxStart = startOfTodayPacific.plusDays(25).withHourOfDay(11)
        val lxEnd = startOfTodayPacific.plusDays(25).withHourOfDay(17)
        params.put("lxStartEpochSeconds", "" + lxStart.millis / 1000)
        params.put("lxStartTzOffset", "" + pacificTimeZone.getOffset(lxStart.millis) / 1000)
        params.put("lxEndEpochSeconds", "" + lxEnd.millis / 1000)
        params.put("lxEndTzOffset", "" + pacificTimeZone.getOffset(lxEnd.millis) / 1000)


        // Inject package DateTimes
        val pckgStart = startOfTodayPacific.plusDays(35).withHourOfDay(4)
        val pckgEnd = startOfTodayPacific.plusDays(41).withHourOfDay(12)
        val pckgHotelCheckIn = startOfTodayPacific.plusDays(35).withHourOfDay(8).withMinuteOfHour(0)
        var pckgHotelCheckOut: DateTime
        try {
            pckgHotelCheckOut = startOfTodayPacific.plusDays(40).withHourOfDay(2).withMinuteOfHour(0)
        } catch (e: IllegalArgumentException) {
            pckgHotelCheckOut = startOfTodayPacific.plusDays(40).withHourOfDay(3).withMinuteOfHour(0)

        }
        val pckgOutboundFlightDeparture = startOfTodayPacific.plusDays(35).withHourOfDay(4)
        val pckgOutboundFlightArrival = startOfTodayPacific.plusDays(35).withHourOfDay(6).withMinuteOfHour(4)
        val pckgInboundFlightDeparture = startOfTodayPacific.plusDays(40).withHourOfDay(10)
        val pckgInboundFlightArrival = startOfTodayPacific.plusDays(40).withHourOfDay(12)
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

        // Inject rails DateTimes
        val railStart = startOfTodayPacific.plusDays(44).withHourOfDay(4)
        val railEnd = startOfTodayPacific.plusDays(48).withHourOfDay(12)
        params.put("railStartEpochSeconds", "" + railStart.millis / 1000)
        params.put("railStartTzOffset", "" + pacificTimeZone.getOffset(railStart.millis) / 1000)
        params.put("railEndEpochSeconds", "" + railEnd.millis / 1000)
        params.put("railEndTzOffset", "" + pacificTimeZone.getOffset(railEnd.millis) / 1000)

        return makeResponse("/api/trips/happy.json", params)
    }

    private fun dispatchUpgradeOffersResponse(): MockResponse {
        return makeResponse("/api/trips/upgradeOffers/happy.json", emptyMap())
    }

    private fun dispatchSuggest(request: RecordedRequest): MockResponse {
        var type: String? = ""
        var regionType: String? = ""
        var latlong: String? = ""
        var lob: String? = ""
        val params = parseHttpRequest(request)
        if (params.containsKey("type")) {
            type = params["type"]
        }
        if (params.containsKey("regiontype")) {
            regionType = params["regiontype"]
        }
        if (params.containsKey("latlong")) {
            latlong = params["latlong"]
        }
        if (params.containsKey("lob")) {
            lob = params["lob"]
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
            if ("FLIGHTS".equals(lob)) {
                //Material Flights
                if(request.path.startsWith("/api/v4/typeahead/lon?")) {
                    return makeResponse("/api/v4/suggestion_flights_lon.json")
                }
                return makeResponse("/api/v4/suggestion_flights.json")
            } else {
                val requestPath = request.path
                val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?')).toLowerCase()
                if ("Flights".equals(lob, false)) {
                    return makeResponse("/api/v4/suggestion_" + unUrlEscape(filename) + ".json")
                } else {
                    return makeResponse("/api/v4/suggestion.json")
                }
            }
        }
        return make404()
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
            return makeResponse("/api/gaia/nearby_gaia_suggestion.json");
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "hotels") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result.json");
        }
        if ((latitude == "0.0") && (longitude == "0.0") && lob == "hotels") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_zero_results.json");
        }
        if ((latitude == "3.0") && (longitude == "3.0") && lob == "lx") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_lx.json");
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "lx" && locale == "fr_FR") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result_lx_french.json");
        }
        if ((latitude == "1.0") && (longitude == "1.0") && lob == "lx" && locale == "en_US") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_single_result_lx_english.json");
        }
        if ((latitude == "0.0") && (longitude == "0.0") && lob == "lx") {
            return makeResponse("/api/gaia/nearby_gaia_suggestion_with_zero_results.json");
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

    private fun dispatchStaticContent(request: RecordedRequest): MockResponse {
        return makeResponse(request.path)
    }

    private fun dispatchUserProfile(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        return makeResponse("api/user/profile/user_profile_" + params["tuid"] + ".json")
    }

    private fun makeResponse(fileName: String, params: Map<String, String>? = null, responseCode: Int = 200): MockResponse {
        return makeResponse(fileName, params, fileOpener, responseCode)
    }

    private fun dispatchTravelAd(endPoint: String): MockResponse {
        val count = travelAdRequests[endPoint] ?: 0
        travelAdRequests.put(endPoint, count + 1)
        return makeEmptyResponse();
    }

    private fun dispatchReviews(): MockResponse {
        return makeResponse("api/hotelreviews/hotel/happy.json")
    }

    private fun dispatchCalculatePoints(request: RecordedRequest): MockResponse {
        val params = parseHttpRequest(request)
        val tripParams = params["tripId"]?.split("|") ?: listOf(params["tripId"])
        val response = makeResponse("/m/api/trip/calculatePoints/"+ tripParams[0] +".json")
        if (tripParams.size > 1) {
            response.setBodyDelay(tripParams[1]?.toLong() ?: 0  , TimeUnit.MILLISECONDS)
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

    fun numOfTravelAdRequests(key: String): Int {
        return travelAdRequests[key] ?: 0
    }
}

