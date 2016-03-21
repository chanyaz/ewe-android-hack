package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

class FlightApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path
        val params = parseRequest(request)

        if (!FlightApiRequestMatcher.isFlightApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        if (!FlightApiRequestMatcher.isRequestContainsClientId(params)) {
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
            val isPassportNeeded = params["departureAirport"] == "PEN" && params["arrivalAirport"] == "KUL"
            val isReturnFlightSearch = params.containsKey("returnDate")
            val departureDate = params["departureDate"]
            val filename = if (isReturnFlightSearch) {
                                "happy_roundtrip"
                            } else if (isPassportNeeded) {
                                "passport_needed_oneway"
                            } else {
                                "happy_oneway"
                            }

            val departCalTakeoff = parseYearMonthDay(departureDate, 10, 0)
            val departCalLanding = parseYearMonthDay(departureDate, 12 + 4, 0)
            params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.timeInMillis / 1000))
            params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.timeInMillis / 1000))

            if (isReturnFlightSearch) {
                val returnDate = params["returnDate"]
                val returnCalTakeoff = parseYearMonthDay(returnDate, 10, 0)
                val returnCalLanding = parseYearMonthDay(returnDate, 12 + 4, 0)
                params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.timeInMillis / 1000))
                params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.timeInMillis / 1000))
            }
            params.put("tzOffsetSeconds", "" + (departCalTakeoff.timeZone.getOffset(departCalTakeoff.timeInMillis) / 1000))

            return "api/flight/search/$filename.json"
        }

        fun getCheckoutResponseFilePath(params: MutableMap<String, String>): String {
            val tripId = params["tripId"] ?: throw RuntimeException("tripId required")
            val tealeafTransactionId = params["tealeafTransactionId"] ?: throw RuntimeException("teleafTransactionId required")

            if ("tealeafFlight:" + tripId != tealeafTransactionId) {
                throw RuntimeException("tripId must match tealeafTransactionId ('tealeafFlight:<tripId>') got: $tealeafTransactionId")
            }

            val isRequestingAirAttachMockResponse = FlightApiRequestMatcher.doesItMatch("^air_attach_0$", tripId)

            if (isRequestingAirAttachMockResponse) {
                val c = Calendar.getInstance()
                c.time = Date()
                c.add(Calendar.DATE, 10)
                val millisFromEpoch = (c.timeInMillis / 1000)
                val tzOffsetSeconds = (c.timeZone.getOffset(c.timeInMillis) / 1000)
                params.put("airAttachEpochSeconds", "" + millisFromEpoch)
                params.put("airAttachTimeZoneOffsetSeconds", "" + tzOffsetSeconds)
            }

            return "api/flight/checkout/" + params["tripId"] + ".json"
        }

        fun getCreateTripResponseFilePath(params: MutableMap<String, String>): String {
            return "api/flight/trip/create/" + params["productKey"] + ".json"
        }
    }
}

class FlightApiRequestMatcher() {
    companion object {
        fun isRequestContainsClientId(params: MutableMap<String, String>): Boolean {
            return params.containsKey("clientid")
        }

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
