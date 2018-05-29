package com.mobiata.mocke3

import com.expedia.bookings.data.ApiError
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.HashMap
import java.util.Calendar
import java.util.Date

class FlightApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val requestBody = getRequestBody(request)

        val urlPath = request.path

        val params = parseHttpRequest(request)

        if (!FlightApiRequestMatcher.isFlightApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        if (!FlightApiRequestMatcher.isRequestContainsClientId(params, urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            FlightApiRequestMatcher.isSearchRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getSearchResponseFilePath(params), params)

            FlightApiRequestMatcher.isOldCreateTripRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCreateTripResponseFilePath(params), params)

            FlightApiRequestMatcher.isNewCreateTripRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCreateTripResponseFilePath(params), params)

            FlightApiRequestMatcher.isCheckoutRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCheckoutResponseFilePath(requestBody, params), params)

            else -> make404()
        }
    }

    private fun getRequestBody(request: RecordedRequest): String {
        val output = ByteArrayOutputStream()
        request.body.copyTo(output)

        val input = ByteArrayInputStream(output.toByteArray())
        val sb = StringBuilder()
        val br = BufferedReader(InputStreamReader(input))
        var line: String? = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        return sb.toString();
    }
}

class FlightApiMockResponseGenerator() {

    companion object {
        val createTripCustomErrorRequestCodes by lazy {
            val hashMap = HashMap<String, ApiError.Code>()
            hashMap.put(TRIP_ALREADY_BOOKED, ApiError.Code.TRIP_ALREADY_BOOKED)
            hashMap.put(PAYMENT_FAILED, ApiError.Code.PAYMENT_FAILED)
            hashMap.put(UNKNOWN_ERROR, ApiError.Code.UNKNOWN_ERROR)
            hashMap.put(SESSION_TIMEOUT, ApiError.Code.SESSION_TIMEOUT)
            hashMap.put(INVALID_INPUT, ApiError.Code.INVALID_INPUT)
            hashMap
        }


        val TRIP_ALREADY_BOOKED = "tripalreadybooked"
        val PAYMENT_FAILED = "paymentfailederror"
        val UNKNOWN_ERROR = "unknownerror"
        val SESSION_TIMEOUT = "sessiontimeout"
        val INVALID_INPUT = "invalidinput"
        val TRIP_ID = "tripId"

        fun getSearchResponseFilePath(params: MutableMap<String, String>): String {
            val departureAirport = params["departureAirport"]
            val suggestionResponseType = FlightDispatcherUtils.SuggestionResponseType.getValueOf(departureAirport!!)

            val isReturnFlightSearch = params.containsKey("returnDate")
            val departureDate = params["departureDate"]
            val legNo = params["ul"]

            val fileName =
                  if (isReturnFlightSearch) {
                        if (legNo != null && legNo.equals("0")) {
                            suggestionResponseType.suggestionString + "_outbound"
                        } else if (legNo != null && legNo.equals("1")) {
                            suggestionResponseType.suggestionString + "_inbound"
                        } else {
                            suggestionResponseType.suggestionString + "_round_trip"
                        }
                    } else {
                        suggestionResponseType.suggestionString + "_one_way"
                    }

            val departCalTakeoff = parseYearMonthDay(departureDate, 10, 0)
            val departCalLanding = parseYearMonthDay(departureDate, 12 + 4, 0)
            params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.timeInMillis / 1000))
            params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.timeInMillis / 1000))

            if (isReturnFlightSearch || suggestionResponseType == FlightDispatcherUtils.SuggestionResponseType.MAY_CHARGE_OB_FEES) {
                val returnDate = params["returnDate"]
                val returnCalTakeoff = parseYearMonthDay(returnDate, 10, 0)
                val returnCalLanding = parseYearMonthDay(returnDate, 12 + 4, 0)
                params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.timeInMillis / 1000))
                params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.timeInMillis / 1000))
            }
            params.put("tzOffsetSeconds", "" + (departCalTakeoff.timeZone.getOffset(departCalTakeoff.timeInMillis) / 1000))

            return "api/flight/search/$fileName.json"
        }

        fun getCheckoutResponseFilePath(requestBody: String, params: MutableMap<String, String>): String {
            val checkoutError = getCheckoutError(requestBody);
            val fileName = if (checkoutError != null) {
                params.put(TRIP_ID, checkoutError.toString())
                "checkout_custom_error"
            } else if (requestBody.contains("checkoutpricechangewithinsurance")) {
                "checkout_price_change_with_insurance"
            } else if (requestBody.contains("checkoutpricechange")) {
                "checkout_price_change"
            } else {
                val tripId = params[TRIP_ID] ?: throw RuntimeException("tripId required")
                val tealeafTransactionId = params["tealeafTransactionId"] ?: throw RuntimeException("teleafTransactionId required")

                if ("tealeafFlight:" + tripId != tealeafTransactionId) {
                    throw RuntimeException("tripId must match tealeafTransactionId ('tealeafFlight:<tripId>') got: $tealeafTransactionId")
                }
                val isRequestingAirAttachMockResponse = doesItMatch("^air_attach_0$", tripId)
                if (isRequestingAirAttachMockResponse) {
                    val c = Calendar.getInstance()
                    c.time = Date()
                    c.add(Calendar.DATE, 10)
                    val millisFromEpoch = (c.timeInMillis / 1000)
                    val tzOffsetSeconds = (c.timeZone.getOffset(c.timeInMillis) / 1000)
                    params.put("airAttachEpochSeconds", "" + millisFromEpoch)
                    params.put("airAttachTimeZoneOffsetSeconds", "" + tzOffsetSeconds)
                }
                tripId
            }

            return "api/flight/checkout/$fileName.json"
        }

        private fun getCheckoutError(requestBody: String): ApiError.Code? {
            val keys = createTripCustomErrorRequestCodes.keys
            for (checkoutError in keys) {
                if (requestBody.contains(checkoutError)) {
                    return createTripCustomErrorRequestCodes[checkoutError]
                }
            }
            return null
        }

        fun getCreateTripResponseFilePath(params: MutableMap<String, String>): String {
            val productKey = params["productKey"]!!
            val isErrorCode = isCreateTripErrorCodeResponse(productKey)

            if (isErrorCode) {
                return "api/flight/trip/create/custom_error_create_trip.json"
            } else {
                return "api/flight/trip/create/$productKey.json"
            }
        }

        private fun isCreateTripErrorCodeResponse(code: String): Boolean {
            try {
                ApiError.Code.valueOf(code)
                return true
            } catch (e: IllegalArgumentException) {
                return false
            }
        }

    }
}

class FlightApiRequestMatcher() {
    companion object {
        fun isRequestContainsClientId(params: MutableMap<String, String>, urlPath: String): Boolean {
            return params.containsKey("clientid") || doesItMatch(".*clientid.*", urlPath)
        }

        fun isFlightApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/.*$", urlPath)
        }

        fun isSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/search.*$", urlPath)
        }

        fun isOldCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/trip/create.*$", urlPath)
        }

        fun isNewCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/trip.*$", urlPath)
        }

        fun isCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/checkout.*$", urlPath)
        }
    }
}
