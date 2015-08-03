package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

public class FlightApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener: FileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.getPath()
        val params = parseRequest(request)

        if (!FlightApiRequestMatcher.isFlightApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            FlightApiRequestMatcher.isSearchRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getSearchResponseFilePath(params), params)

            FlightApiRequestMatcher.isCreateTripRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCreateTripResponseFilePath(params), params)

            FlightApiRequestMatcher.isCheckoutRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCheckoutResponseFilePath(params), params)

            else -> make404()
        }
    }
}

class FlightApiMockResponseGenerator() {
    companion object {
        fun getSearchResponseFilePath(params: MutableMap<String, String>): String {
            val isReturnFlightSearch = params.containsKey("returnDate") ?: throw UnsupportedOperationException("Expected returnDate parameter")
            val departureDate = params.get("departureDate")
            val filename = if (isReturnFlightSearch) "happy_roundtrip" else "happy_oneway"

            val departCalTakeoff = parseYearMonthDay(departureDate, 10, 0)
            val departCalLanding = parseYearMonthDay(departureDate, 12 + 4, 0)
            params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.getTimeInMillis() / 1000))
            params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.getTimeInMillis() / 1000))

            if (isReturnFlightSearch) {
                val returnDate = params.get("returnDate")
                val returnCalTakeoff = parseYearMonthDay(returnDate, 10, 0)
                val returnCalLanding = parseYearMonthDay(returnDate, 12 + 4, 0)
                params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.getTimeInMillis() / 1000))
                params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.getTimeInMillis() / 1000))
            }
            params.put("tzOffsetSeconds", "" + (departCalTakeoff.getTimeZone().getOffset(departCalTakeoff.getTimeInMillis()) / 1000))

            return "api/flight/search/" + filename + ".json"
        }

        fun getCheckoutResponseFilePath(params: MutableMap<String, String>): String {
                val tripId = params.get("tripId") ?: throw UnsupportedOperationException("Expected tripId parameter")
                val isRequestingAirAttachMockResponse = FlightApiRequestMatcher.doesItMatch("^air_attach_0$", tripId)

                if (isRequestingAirAttachMockResponse) {
                    val c = Calendar.getInstance()
                    c.setTime(Date())
                    c.add(Calendar.DATE, 10)
                    val millisFromEpoch = (c.getTimeInMillis() / 1000)
                    val tzOffsetSeconds = (c.getTimeZone().getOffset(c.getTimeInMillis()) / 1000)
                    params.put("airAttachEpochSeconds", "" + millisFromEpoch)
                    params.put("airAttachTimeZoneOffsetSeconds", "" + tzOffsetSeconds)
                }

                return "api/flight/checkout/" + params.get("tripId") + ".json"
        }

        fun getCreateTripResponseFilePath(params: MutableMap<String, String>): String {
            return "api/flight/trip/create/" + params.get("productKey") + ".json"
        }
    }
}

class FlightApiRequestMatcher() {
    companion object {
        fun isFlightApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/.*$", urlPath)
        }

        fun isSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/search.*$", urlPath)
        }

        fun isCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/trip/create$", urlPath)
        }

        fun isCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/checkout$", urlPath)
        }

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }
}
