package com.mobiata.mocke3

import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test
import org.mockito.Mockito
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpediaDispatcherTests {
    @Test(expected = UnsupportedOperationException::class)
    fun testRequestWithoutValidUserAgentThrowsException() {
        val request = RecordedRequest(null, Headers.of("test", "value"), null, 0, null, 0, null)
        defaultDispatcher.dispatch(request)
    }

    @Test
    fun testUnmatchedPathReturns404Response() {
        val mock = mockForRequest(requestWithPath("UnmatchedPath"), defaultDispatcher)

        assertTrue { mock.status == "HTTP/1.1 404 Client Error" }
        assertNull(mock.body)
    }

    @Test
    fun testConfigFeaturePathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("m/api/config/feature", null, "m/api/satellite/featureConfig.json")
    }

    @Test
    fun testRequestWithCardFeePathReturnMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("api/flight/trip/cardFee", null, "api/flight/trip/cardFee/happy.json")
    }

    @Test
    fun testRequestWithRailsPathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("rails/trip/cardFee", null, "m/api/rails/trip/cardfee/visa.json")
    }

    @Test
    fun testRequestWithPackagesV1PathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("getpackages/v1", mapOf(Pair("ftla", "")), "getpackages/v1/happy.json")
    }

    @Test
    fun testRequestWithPackagesV2PathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("api/packages/createTrip", mapOf(Pair("productKey", "create_trip")), "api/packages/createtrip/create_trip.json")
    }

    @Test
    fun testRequestWithMultiItemPathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("api/multiitem/v1/hotels", mapOf(Pair("origin", "happy")), "api/multiitem/v1/happy.json")
    }

    @Test
    fun testRequestWithHotelPathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("m/api/hotel/search", mapOf(Pair("regionId", "happy")), "m/api/hotel/search/happy.json")
    }

    @Test
    fun testRequestWithHotelCouponPathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("api/m/trip/coupon", mapOf(Pair("coupon.code", "hotel_coupon_success")), "api/m/trip/coupon/hotel_coupon_success.json")
    }

    @Test
    fun testRequestWithHotelRemoveCouponPathReturnsMatchingMockResponse() {
        assertPathReturnsMockBodyWithString("api/m/trip/remove/coupon", mapOf(Pair("tripId", "happypath_coupon_remove_success")), "api/m/trip/remove/coupon/happypath_coupon_remove_success.json")
    }

    @Test
    fun testRequestWithFlightPathReturnsMatchingResponse() {
        val params = mapOf(Pair("clientid", "test"), Pair("departureAirport", "happy"), Pair("departureDate", "1-19-1977"))
        assertPathReturnsMockBodyWithString("api/flight/search", params, "api/flight/search/happy_one_way.json")
    }

    @Test
    fun testRequestWithLXSearchPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("lx/api/search", null, "lx/api/search/happy.json")
    }

    @Test
    fun testRequestWithLXSearchCheckoutPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("m/api/lx/trip/checkout", mapOf(Pair("tripId", "happypath_trip_id")), "m/api/lx/trip/checkout/happypath_trip_id.json")
    }

    @Test
    fun testRequestWithSOSPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("sos/offers/member-only-deals", null, "m/api/sos/sos.json")
    }

    @Test
    fun testRequestWithEvaluateExperimentsReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/bucketing/v1/evaluateExperiments", mapOf(Pair("tpid", "1")), "api/bucketing/happy1.json")
    }

    @Test
    fun testRequestWithLogExperimentsPathReturnsEmptyResponse() {
        assertEmptyResponseForPath("api/bucketing/v1/logExperiments")
    }

    @Test
    fun testRequestWithTripsPathContainingParametersReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/trips", mapOf(Pair("key", "value")), "api/trips/happy.json")
    }

    @Test
    fun testRequestWithTripsPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/trips/happy", null, "api/trips/happy.json")
    }

    @Test
    fun testRequestWithTripsCalculatePointsPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/trip/calculatePoints", mapOf(Pair("tripId", "happy")), "m/api/trip/calculatePoints/happy.json")
    }

    @Test
    fun testRequestWithTripsCalculatePointsPathAndDelayReturnsResponseWithPopulatedDelay() {
        val response = defaultDispatcher.dispatch(requestWithPath("api/trip/calculatePoints?tripId=happy|1"))

        assertEquals(1, response.getBodyDelay(TimeUnit.MILLISECONDS))
    }

    @Test
    fun testRequestWithTripsErrorTripResponsePathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/trips/error_trip_response", mapOf(Pair("key", "value")), "api/trips/error_trip_response.json")
    }

    @Test
    fun testRequestWithTripsErrorBadRequestTripResponsePathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/trips/error_bad_request_trip_response", mapOf(Pair("key", "value")), "api/trips/error_bad_request_trip_response.json")
    }

    @Test
    fun testRequestWithTripsPathAndEmailReturnsMatchingResponse() {
        val dispatcher = defaultDispatcher
        val emailRequest = requestWithPath("api/user/sign-in?email=trip_error@mobiata.com")

        dispatcher.dispatch(emailRequest)

        val parameters = mapOf(Pair("key", "value"))
        assertPathReturnsMockBodyWithString("api/trips", parameters, "api/trips/error_trip_response.json", dispatcher)
    }

    @Test
    fun testRequestWithV2SuggestPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("hint/es/v2/ac/en_US/suggestion", mapOf(Pair("key", "value")), "hint/es/v2/ac/en_US/suggestion.json")
    }

    @Test
    fun testRequestWithV3SuggestPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("hint/es/v3/ac/en_US/", null, "hint/es/v3/ac/en_US/suggestion.json")
    }

    @Test
    fun testRequestWithV3SuggestPathAndTypeReturnsSuggestionCityResponse() {
        val parameters = mapOf(Pair("type", "14"))
        assertPathReturnsMockBodyWithString("hint/es/v3/ac/en_US/", parameters, "hint/es/v3/ac/en_US/suggestion_city.json")
    }

    @Test
    fun testRequestWithV1SuggestPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("hint/es/v1/nearby/en_US/", null, "hint/es/v1/nearby/en_US/suggestion.json")
    }

    @Test
    fun testRequestWithV1SuggestPathAndLatLongReturnsMatchingResponse() {
        val parameters = mapOf(Pair("latlong", "31.32|75.57"))
        assertPathReturnsMockBodyWithString("hint/es/v1/nearby/en_US/", parameters, "hint/es/v1/nearby/en_US/suggestion_with_no_lx_activities.json")
    }

    @Test
    fun testRequestWithV1SuggestPathAndTypeReturnsMatchingResponse() {
        val parameters = mapOf(Pair("type", "14"))
        assertPathReturnsMockBodyWithString("hint/es/v1/nearby/en_US/", parameters, "hint/es/v1/nearby/en_US/suggestion_city.json")
    }

    @Test
    fun testRequestWithV4TypeaheadPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/v4/typeahead/", mapOf(Pair("lob", "Hotels")), "api/v4/suggestion.json")
    }

    @Test
    fun testRequestWithV4TypeaheadPathAndFlightsLOBReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/v4/typeahead/", mapOf(Pair("lob", "FLIGHTS")), "api/v4/suggestion_flights.json")
    }

    @Test
    fun testRequestWithV4TypeaheadLonPathAndFlightsLOBReturnsMatchingResponse() {
        val parameters = mapOf(Pair("lob", "FLIGHTS"))
        assertPathReturnsMockBodyWithString("api/v4/typeahead/lon", parameters, "api/v4/suggestion_flights_lon.json")
    }

    @Test
    fun testRequestWithV4TypeaheadPathAndPackagesLOBReturnsMatchingResponse() {
        val parameters = mapOf(Pair("lob", "PACKAGES"))
        assertPathReturnsMockBodyWithString("api/v4/typeahead/", parameters, "api/v4/suggestion.json")
    }

    @Test
    fun testRequestWithV4TypeaheadDelPathAndPackagesLOBReturnsMatchingResponse() {
        val parameters = mapOf(Pair("lob", "PACKAGES"))
        assertPathReturnsMockBodyWithString("api/v4/typeahead/del", parameters, "api/v4/suggestion_packages_del.json")
    }

    @Test
    fun testRequestWithV4TypeaheadForNYCPathAndFlightsLOBReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/v4/typeahead/nyc", mapOf(Pair("lob", "Flights")), "api/v4/suggestion_nyc.json")
    }

    @Test
    fun testUnmatchedESSGaiaPathReturns404Response() {
        val mock = mockForRequest(requestWithPath("hint/es/v1/unmatched"), defaultDispatcher)

        assertTrue { mock.status == "HTTP/1.1 404 Client Error" }
        assertNull(mock.body)
    }

    @Test
    fun testRequestWithFeaturesPathReturnsNoLXActivitiesResponse() {
        assertPathReturnsMockBodyWithString("features/", mapOf(Pair("lat", "31.32"), Pair("lng", "75.57")), "api/gaia/nearby_gaia_suggestion_with_no_lx_activities.json")
    }

    @Test
    fun testRequestWithFeaturesHotelsPathReturnsNearbyGaiaResponse() {
        val parameters = mapOf(Pair("lat", "3.0"), Pair("lng", "3.0"), Pair("lob", "hotels"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion.json")
    }

    @Test
    fun testRequestWithFeaturesHotelsPathReturnsNearbySingleResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "1.0"), Pair("lng", "1.0"), Pair("lob", "hotels"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_single_result.json")
    }

    @Test
    fun testRequestWithFeaturesHotelsPathReturnsNearbyZeroResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "0.0"), Pair("lng", "0.0"), Pair("lob", "hotels"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_zero_results.json")
    }

    @Test
    fun testRequestWithFeaturesLXPathReturnsNearbyGaiaResponse() {
        val parameters = mapOf(Pair("lat", "3.0"), Pair("lng", "3.0"), Pair("lob", "lx"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_lx.json")
    }

    @Test
    fun testRequestWithFeaturesLXPathAndFrenchLocaleReturnsFrenchNearbySingleResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "1.0"), Pair("lng", "1.0"), Pair("lob", "lx"), Pair("locale", "fr_FR"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_single_result_lx_french.jso")
    }

    @Test
    fun testRequestWithFeaturesLXPathAndEnglishLocaleReturnsFrenchNearbySingleResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "1.0"), Pair("lng", "1.0"), Pair("lob", "lx"), Pair("locale", "en_US"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_single_result_lx_english.js")
    }

    @Test
    fun testRequestWithFeaturesLXPathReturnsNearbyZeroResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "0.0"), Pair("lng", "0.0"), Pair("lob", "lx"), Pair("locale", "en_US"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_zero_results.json")
    }

    @Test
    fun testRequestWithFeaturesPathReturnsNearbyZeroResultGaiaResponse() {
        val parameters = mapOf(Pair("lat", "0.0"), Pair("lng", "0.0"), Pair("lob", "lx"), Pair("locale", "en_US"))
        assertPathReturnsMockBodyWithString("features/", parameters, "api/gaia/nearby_gaia_suggestion_with_zero_results.json")
    }

    @Test
    fun testUnmatchedFeaturesPathReturns404Response() {
        val mock = mockForRequest(requestWithPath("features/"), defaultDispatcher)

        assertTrue { mock.status == "HTTP/1.1 404 Client Error" }
        assertNull(mock.body)
    }

    @Test
    fun testRequestWithSignInPathReturnsMatchingReponse() {
        assertPathReturnsMockBodyWithString("api/user/sign-in", null, "api/user/sign-in/qa-ehcc@mobiata.com.json")
    }

    @Test
    fun testRequestWithInsurancePathReturnsMatchingResponse() {
        val parameters = mapOf(Pair("tripId", "happy_round_trip_with_insurance_available"))
        assertPathReturnsMockBodyWithString("m/api/insurance", parameters, "api/flight/trip/create/happy_round_trip_with_insurance_available")
    }

    @Test
    fun testRequestWithInsurancePathAndProductIdReturnsMatchingResponse() {
        val parameters = mapOf(Pair("tripId", "happy_round_trip_with_insurance_available"), Pair("insuranceProductId", "0"))
        assertPathReturnsMockBodyWithString("m/api/insurance", parameters, "api/flight/trip/create/happy_round_trip_with_insurance_selected")
    }

    @Test
    fun testRequestWithOmniturePathReturnsEmptyResponse() {
        assertEmptyResponseForPath("b/ss")
    }

    @Test
    fun testRequestWithStaticContentPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("static/mobile/PhoneDestinations/us/collections_default.json", null, "static/mobile/PhoneDestinations/us/collections_default.json")
    }

    @Test
    fun testRequestWithUserProfilePathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/user/profile", mapOf(Pair("tuid", "0")), "api/user/profile/user_profile_0.json")
    }

    @Test
    fun testRequestWithTravelAdImpressionPathReturnsEmptyResponse() {
        assertEmptyResponseForPath("TravelAdsService/v3/Hotels/TravelAdImpression")
    }

    @Test
    fun testRequestWithTravelAdClickPathReturnsEmptyResponse() {
        assertEmptyResponseForPath("TravelAdsService/v3/Hotels/TravelAdClick")
    }

    @Test
    fun testRequestWithTravelAdPathReturnsEmptyResponse() {
        assertEmptyResponseForPath("travel")
    }

    @Test
    fun testRequestWithTravelAdHookLogicPathReturnsEmptyResponse() {
        assertEmptyResponseForPath("ads/hooklogic")
    }

    @Test
    fun testRequestWithHotelReviewsPathReturnsMatchingResponse() {
        assertPathReturnsMockBodyWithString("api/hotelreviews", null, "api/hotelreviews/hotel/happy.json")
    }

    @Test
    fun testNumOfTravelAdRequestsEquality() {
        val dispatcher = defaultDispatcher
        dispatcher.dispatch(requestWithPath("TravelAdsService/v3/Hotels/TravelAdImpression"))
        dispatcher.dispatch(requestWithPath("TravelAdsService/v3/Hotels/TravelAdClick"))


        assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"))
        assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"))
        assertEquals(0, dispatcher.numOfTravelAdRequests("/travel"))
    }

    private fun assertEmptyResponseForPath(path: String) {
        assertTrue(path.isNotEmpty(), "path should not be empty.")

        val mock = mockForRequest(requestWithPath(path), defaultDispatcher)

        assertTrue { mock.status == "HTTP/1.1 200 OK" }
        assertNull(mock.body)
    }

    private fun assertPathReturnsMockBodyWithString(path: String, parameters: Map<String, String>?, expectedBodyString: String, dispatcher: ExpediaDispatcher = defaultDispatcher) {
        var compiledPath = path

        if (parameters != null) {
            compiledPath = StringBuilder("$compiledPath?").append(parameters.map { it.key + "=" + it.value }.joinToString("&")).toString()
        }

        assertTrue(expectedBodyString.isNotEmpty(), "expectedBodyString should not be empty.")
        assertTrue(mockForRequest(requestWithPath(compiledPath), dispatcher).body.toString().contains(expectedBodyString, false), "Mock response body for \"$path\" does not contain \"$expectedBodyString\"")
    }

    private fun mockForRequest(request: RecordedRequest, dispatcher: ExpediaDispatcher): MockResponse {
        val mock = dispatcher.dispatch(request)

        return mock
    }

    private fun requestWithPath(path: String): RecordedRequest {
        val headers = Headers.of("user-agent", "ExpediaBookings/0.0.0 (EHad; Mobiata)")
        val testSocket = Mockito.mock(Socket::class.java)

        Mockito.`when`(testSocket.inetAddress).thenReturn(InetAddress.getByName("expedia.com"))
        Mockito.`when`(testSocket.localPort).thenReturn(80)

        return RecordedRequest("GET /$path HTTP/1.1", headers, null, 0, okio.Buffer(), 0, testSocket)
    }

    private val defaultDispatcher: ExpediaDispatcher
        get() = ExpediaDispatcher(Opener())

    private class Opener: FileOpener {
        override fun openFile(filename: String): InputStream = filename.byteInputStream()
    }
}
