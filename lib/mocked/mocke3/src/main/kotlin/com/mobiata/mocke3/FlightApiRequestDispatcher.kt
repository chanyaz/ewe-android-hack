package com.mobiata.mocke3

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.utils.Constants
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
            FlightApiRequestMatcher.isSearchRequest(urlPath) -> {
                val isCachedSearch = request.path.contains(Constants.FEATURE_FLIGHT_CACHE)
                getMockResponse(FlightApiMockResponseGenerator.getSearchResponseFilePath(params, isCachedSearch), params)
            }

            FlightApiRequestMatcher.isCreateTripRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCreateTripResponseFilePath(params), params)

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

    enum class SearchResultsResponseType(val responseName: String) {
        HAPPY_ONE_WAY("happy_one_way"),
        BYOT_ROUND_TRIP("byot_search"),
        HAPPY_ROUND_TRIP("happy_round_trip"),
        HAPPY_ROUND_TRIP_WITH_INSURANCE_AVAILABLE("happy_round_trip_with_insurance_available"),
        CREATE_TRIP_PRICE_CHANGE("create_trip_price_change")
    }

    enum class SuggestionResponseType(val suggestionString: String) {
        HAPPY_PATH("happy"),
        BYOT_ROUND_TRIP("byot_search"),
        PASSPORT_NEEDED("passport_needed"),
        MAY_CHARGE_OB_FEES("may_charge_ob_fees"),
        SEARCH_ERROR("search_error"),
        EARN("earn"),
        CACHED_BOOKABLE("cached_bookable"),
        CACHED_NON_BOOKABLE("cached_non_bookable"),
        CACHED_NOT_FOUND("cached_not_found");


        companion object {

            private val suggestionResponseTypeMap = HashMap<String, SuggestionResponseType>()

            init {
                for (suggestionResponseType in SuggestionResponseType.values()) {
                    suggestionResponseTypeMap.put(suggestionResponseType.suggestionString, suggestionResponseType)
                }
            }

            fun getValueOf(fileName: String): SuggestionResponseType {
                return suggestionResponseTypeMap[fileName] ?: HAPPY_PATH
            }
        }
    }

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

        fun getSearchResponseFilePath(params: MutableMap<String, String>, isCached: Boolean = false): String {
            val departureAirport = params["departureAirport"]
            val suggestionResponseType = SuggestionResponseType.getValueOf(departureAirport!!)

            val isReturnFlightSearch = params.containsKey("returnDate")
            val departureDate = params["departureDate"]
            val legNo = params["ul"]

            val fileName =
                    if (isCached) {
                        suggestionResponseType.suggestionString
                    }
                    else if (isReturnFlightSearch) {
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

            if (isReturnFlightSearch || suggestionResponseType == SuggestionResponseType.MAY_CHARGE_OB_FEES) {
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

        fun isCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/trip/create.*$", urlPath)
        }

        fun isCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/flight/checkout.*$", urlPath)
        }
    }
}
