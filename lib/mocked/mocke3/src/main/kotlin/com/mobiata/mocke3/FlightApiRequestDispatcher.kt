package com.mobiata.mocke3

import com.expedia.bookings.data.ApiError
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.Calendar
import java.util.Date

class FlightApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    enum class SearchResponseType {
        HAPPY_ROUND_TRIP,
        HAPPY_ONE_WAY,
        PASSPORT_NEEDED_ONE_WAY,
        MAY_CHARGE_OB_FEES_ROUND_TRIP,
        CHECKOUT_PRICE_CHANGE,
        CREATETRIP_PRICE_CHANGE,
        SEARCH_ERROR,
        CREATETRIP_UNKNOWN_ERROR,
        CREATETRIP_FLIGHT_SOLD_OUT,
        CREATETRIP_PRODUCT_NOT_FOUND,
        CREATETRIP_SESSION_TIMEOUT,
        CHECKOUT_UNKNOWN_ERROR,
        CHECKOUT_PAYMENT_FAILED,
        CHECKOUT_SESSION_TIMEOUT,
        CHECKOUT_TRIP_ALREADY_BOOKED,
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
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

            FlightApiRequestMatcher.isCreateTripRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCreateTripResponseFilePath(params), params)

            FlightApiRequestMatcher.isCheckoutRequest(urlPath) -> getMockResponse(FlightApiMockResponseGenerator.getCheckoutResponseFilePath(params), params)

            else -> make404()
        }
    }
}

class FlightApiMockResponseGenerator() {
    companion object {
        fun getSearchResponseFilePath(params: MutableMap<String, String>): String {
            val searchError = params["departureAirport"] == "SearchError"
            val mayChargeObFeesFlight = params["departureAirport"] == "AKL"
            val priceChangeCheckout = params["departureAirport"] == "PCC"
            val priceChangeCreateTrip = params["departureAirport"] == "PCT"
            val unknownErrorCreateTrip = params["departureAirport"] == "UnknownErrorCT"
            val flightSoldOutCreateTrip = params["departureAirport"] == "FlightSoldOutCT"
            val flightProductNotFoundCreateTrip = params["departureAirport"] == "FlightProductNotFoundCT"
            val flightSessionTimeoutCreateTrip = params["departureAirport"] == "SessionTimeoutCT"
            val flightUnknownErrorCheckout = params["departureAirport"] == "UnknownErrorCKO"
            val flightPaymentFailedErrorCheckout = params["departureAirport"] == "PaymentFailedCKO"
            val flightSessionTimeoutCheckout = params["departureAirport"] == "SessionTimeoutCKO"
            val flightTripAlreadyBookedCheckout = params["departureAirport"] == "TripAlreadyBookedCKO"
            val isEarnResponse = params["departureAirport"] == "EARN"
            val isPassportNeeded = params["departureAirport"] == "PEN" && params["arrivalAirport"] == "KUL"
            val isReturnFlightSearch = params.containsKey("returnDate")
            val departureDate = params["departureDate"]
            val flightNoResultsToShow = params["departureAirport"] == "SearchNoResult"

            val searchResponseType =
                    if (isPassportNeeded) {
                        FlightApiRequestDispatcher.SearchResponseType.PASSPORT_NEEDED_ONE_WAY
                    }
                    else if (mayChargeObFeesFlight) {
                        FlightApiRequestDispatcher.SearchResponseType.MAY_CHARGE_OB_FEES_ROUND_TRIP
                    }
                    else if (priceChangeCheckout) {
                        FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_PRICE_CHANGE
                    }
                    else if (priceChangeCreateTrip) {
                        FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_PRICE_CHANGE
                    }
                    else if (searchError) {
                        FlightApiRequestDispatcher.SearchResponseType.SEARCH_ERROR
                    }
                    else if (flightProductNotFoundCreateTrip) {
                        FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_PRODUCT_NOT_FOUND
                    }
                    else if (flightSoldOutCreateTrip) {
                        FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_FLIGHT_SOLD_OUT
                    }
                    else if (unknownErrorCreateTrip) {
                        FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_UNKNOWN_ERROR
                    }
                    else if (flightSessionTimeoutCreateTrip) {
                        FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_SESSION_TIMEOUT
                    }
                    else if (flightUnknownErrorCheckout) {
                        FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_UNKNOWN_ERROR
                    }
                    else if (flightPaymentFailedErrorCheckout) {
                        FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_PAYMENT_FAILED
                    }
                    else if (flightSessionTimeoutCheckout) {
                        FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_SESSION_TIMEOUT
                    }
                    else if (flightTripAlreadyBookedCheckout) {
                        FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_TRIP_ALREADY_BOOKED
                    }
                    else if (isReturnFlightSearch) {
                        FlightApiRequestDispatcher.SearchResponseType.HAPPY_ROUND_TRIP
                    }
                    else {
                        FlightApiRequestDispatcher.SearchResponseType.HAPPY_ONE_WAY
                    }

            val filename = getSearchResponseFileName(searchResponseType, isEarnResponse)

            val departCalTakeoff = parseYearMonthDay(departureDate, 10, 0)
            val departCalLanding = parseYearMonthDay(departureDate, 12 + 4, 0)
            params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.timeInMillis / 1000))
            params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.timeInMillis / 1000))

            if (isReturnFlightSearch || mayChargeObFeesFlight) {
                val returnDate = params["returnDate"]
                val returnCalTakeoff = parseYearMonthDay(returnDate, 10, 0)
                val returnCalLanding = parseYearMonthDay(returnDate, 12 + 4, 0)
                params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.timeInMillis / 1000))
                params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.timeInMillis / 1000))
            }
            params.put("tzOffsetSeconds", "" + (departCalTakeoff.timeZone.getOffset(departCalTakeoff.timeInMillis) / 1000))

            return "api/flight/search/$filename.json"
        }

        private fun getSearchResponseFileName(searchType: FlightApiRequestDispatcher.SearchResponseType, isEarnResponse: Boolean): String {
            var fileName = when (searchType) {
                FlightApiRequestDispatcher.SearchResponseType.PASSPORT_NEEDED_ONE_WAY -> "passport_needed_oneway"

                FlightApiRequestDispatcher.SearchResponseType.HAPPY_ROUND_TRIP -> "happy_roundtrip"

                FlightApiRequestDispatcher.SearchResponseType.HAPPY_ONE_WAY -> "happy_oneway"

                FlightApiRequestDispatcher.SearchResponseType.MAY_CHARGE_OB_FEES_ROUND_TRIP -> "roundtrip_maychargeobfees"

                FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_PRICE_CHANGE -> "checkout_price_change"

                FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_PRICE_CHANGE -> "create_trip_price_change"

                FlightApiRequestDispatcher.SearchResponseType.SEARCH_ERROR -> "search_error"

                FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_UNKNOWN_ERROR -> "create_trip_unknown_error"

                FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_FLIGHT_SOLD_OUT -> "create_trip_flight_sold_out"

                FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_PRODUCT_NOT_FOUND -> "create_trip_product_not_found"

                FlightApiRequestDispatcher.SearchResponseType.CREATETRIP_SESSION_TIMEOUT -> "create_trip_session_timeout_error"

                FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_UNKNOWN_ERROR -> "checkout_unknown_error"

                FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_PAYMENT_FAILED -> "checkout_payment_failed"

                FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_SESSION_TIMEOUT -> "checkout_session_timeout"

                FlightApiRequestDispatcher.SearchResponseType.CHECKOUT_TRIP_ALREADY_BOOKED -> "checkout_trip_already_booked"

            }

            if (isEarnResponse) {
                fileName += "_earn"
            }

            return fileName
        }

        fun getCheckoutResponseFilePath(params: MutableMap<String, String>): String {
            val tripId = params["tripId"] ?: throw RuntimeException("tripId required")
            val tealeafTransactionId = params["tealeafTransactionId"] ?: throw RuntimeException("teleafTransactionId required")
            val isError = isErrorCodeResponse(tripId)

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


            val fileName = if (isError) "checkout_custom_error" else tripId
            return "api/flight/checkout/$fileName.json"
        }

        fun getCreateTripResponseFilePath(params: MutableMap<String, String>): String {
            val productKey = params["productKey"]!!
            val isErrorCode = isErrorCodeResponse(productKey)
            val withInsurance = if (params["withInsurance"] == "true") "_with_insurance_available" else ""

            if (isErrorCode) {
                return "api/flight/trip/create/custom_error_create_trip.json"
            }
            else {
                return "api/flight/trip/create/$productKey$withInsurance.json"
            }
        }

        private fun isErrorCodeResponse(code: String): Boolean {
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
