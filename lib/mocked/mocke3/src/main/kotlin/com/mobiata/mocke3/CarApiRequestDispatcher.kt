package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class CarApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path
        val params = parseHttpRequest(request)

        if (!CarApiRequestMatcher.isCarApiRequest(path)) {
            throwUnsupportedRequestException(path)
        }

        return when {
            CarApiRequestMatcher.isSearchRequest(path) -> {
                return when(params["airportCode"]) {
                    "KTM" -> getMockResponse("m/api/cars/search/airport/ktm_no_product.json")

                    "DTW" -> getMockResponse("m/api/cars/search/airport/dtw_invalid_input.json")

                    else -> getMockResponse("m/api/cars/search/airport/happy.json")
                }
            }

            CarApiRequestMatcher.isCreateTripRequest(path) -> {
                val productKey = params["productKey"]
                return when (productKey) {
                    "CreateTripPriceChange" -> getMockResponse("m/api/cars/trip/create/price_change.json")

                    else -> getMockResponse("m/api/cars/trip/create/$productKey.json", params)
                }
            }

            CarApiRequestMatcher.isCheckoutRequest(path) -> {
                val responseType = params["mainMobileTraveler.firstName"]
                return when (responseType) {
                    "AlreadyBooked" -> getMockResponse("m/api/cars/trip/checkout/trip_already_booked.json")
                    "PriceChange" -> getMockResponse("m/api/cars/trip/checkout/price_change.json")
                    "PaymentFailed" -> getMockResponse("m/api/cars/trip/checkout/payment_failed.json")
                    "UnknownError" -> getMockResponse("m/api/cars/trip/checkout/unknown_error.json")
                    "SessionTimeout" -> getMockResponse("m/api/cars/trip/checkout/session_timeout.json")
                    "InvalidInput" -> getMockResponse("m/api/cars/trip/checkout/invalid_input.json")
                    "happy_0" -> getMockResponse("m/api/cars/trip/checkout/happy_0.json")
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
    }
}
