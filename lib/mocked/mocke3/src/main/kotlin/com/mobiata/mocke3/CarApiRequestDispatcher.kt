package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.regex.Pattern

public class CarApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener: FileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.getPath()
        val params = parseRequest(request)

        if (!CarApiRequestMatcher.isCarApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            CarApiRequestMatcher.isSearchRequest(urlPath) -> {
                return when(params.get("airportCode")) {
                    "KTM" -> getMockResponse("m/api/cars/search/airport/ktm_no_product.json")

                    "DTW" -> getMockResponse("m/api/cars/search/airport/dtw_invalid_input.json")

                    else -> getMockResponse("m/api/cars/search/airport/happy.json")
                }
            }

            CarApiRequestMatcher.isCreateTripRequest(urlPath) -> {
                val productKey = params.get("productKey")
                return when (productKey) {
                    "CreateTripPriceChange" -> getMockResponse("m/api/cars/trip/create/price_change.json")

                    else -> getMockResponse("m/api/cars/trip/create/" + productKey + ".json", params)
                }
            }

            CarApiRequestMatcher.isCheckoutRequest(urlPath) -> {
                val responseType = params.get("mainMobileTraveler.firstName")
                return when (responseType) {
                    "AlreadyBooked" -> getMockResponse("m/api/cars/trip/checkout/trip_already_booked.json")
                    "PriceChange" -> getMockResponse("m/api/cars/trip/checkout/price_change.json")
                    "PaymentFailed" -> getMockResponse("m/api/cars/trip/checkout/payment_failed.json")
                    "UnknownError" -> getMockResponse("m/api/cars/trip/checkout/unknown_error.json")
                    "SessionTimeout" -> getMockResponse("m/api/cars/trip/checkout/session_timeout.json")
                    "InvalidInput" -> getMockResponse("m/api/cars/trip/checkout/invalid_input.json")
                    else -> getMockResponse("m/api/cars/trip/checkout/happy.json")
                }
            }

            else -> make404()
        }
    }
}

class CarApiRequestMatcher() {
    companion object {
        fun isCarApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/cars/.*$", urlPath)
        }

        fun isSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/cars/search/airport.*$", urlPath) || doesItMatch("^/m/api/cars/search/location.*$", urlPath)
        }

        fun isCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/cars/trip/create.*$", urlPath)
        }

        fun isCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/cars/trip/checkout.*$", urlPath)
        }

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }
}
