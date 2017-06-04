package com.mobiata.mocke3

import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.requests.RailCreateTripRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.google.gson.GsonBuilder
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class RailApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {
    var isRoundTrip = false

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!RailApiRequestMatcher.isRailApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            RailApiRequestMatcher.isRailApiSearchRequest(urlPath) -> {
                val gson = GsonBuilder().create()
                val searchParams = gson.fromJson(request.body.readUtf8(), RailApiSearchModel::class.java)
                when(searchParams.clientCode) {
                    "no_search_results" -> getMockResponse("m/api/rails/shop/no_search_results.json")
                    "validation_error" -> getMockResponse("m/api/rails/shop/validation_error.json")
                    else ->
                        if (searchParams.isSearchRoundTrip) {
                            isRoundTrip = true
                            getMockResponse("m/api/rails/shop/roundtrip_happy.json")
                        } else {
                            isRoundTrip = false
                            getMockResponse("m/api/rails/shop/oneway_happy.json")
                        }
                }
            }

            RailApiRequestMatcher.isRailApiCreateTripRequest(urlPath) -> {
                if (isRoundTrip) {
                    getMockResponse("m/api/rails/trip/create/roundtrip_happy.json")
                } else {
                    val gson = GsonBuilder().create()
                    val params = gson.fromJson(request.body.readUtf8(), RailCreateTripRequest::class.java)
                    when(params.offerTokens[0]) {
                        "price_change" -> getMockResponse("m/api/rails/trip/checkout/price_change.json")
                        "validation_errors" -> getMockResponse("m/api/rails/trip/create/validation_error.json")
                        "other_errors" -> getMockResponse("m/api/rails/trip/unknown_error.json")
                        else -> getMockResponse("m/api/rails/trip/create/oneway_happy.json")
                    }
                }
            }

            RailApiRequestMatcher.isRailApiCheckoutRequest(urlPath) -> {
                val gson = GsonBuilder().create()
                val checkoutParams = gson.fromJson(request.body.readUtf8(), RailCheckoutParams::class.java)

                if ("pricechange".equals(checkoutParams.travelers[0].firstName, true)) {
                    getMockResponse("m/api/rails/trip/checkout/price_change.json")
                } else if ("invalidinput".equals(checkoutParams.travelers[0].firstName, true)) {
                    getMockResponse("m/api/rails/trip/checkout/invalid_payment_error.json")
                } else if ("unknownpayment".equals(checkoutParams.travelers[0].firstName, true)) {
                    getMockResponse("m/api/rails/trip/checkout/unknown_payment_error.json")
                } else if ("unknown".equals(checkoutParams.travelers[0].firstName, true)) {
                    make404()
                } else if (isRoundTrip) {
                    getMockResponse("m/api/rails/trip/checkout/roundtrip_happy.json")
                } else {
                    getMockResponse("m/api/rails/trip/checkout/oneway_happy.json")
                }
            }

            RailApiRequestMatcher.isRailApiCardsRequest(urlPath) -> {
                getMockResponse("m/api/rails/shop/cards/en_GB.json")
            }

            RailApiRequestMatcher.isRailApiCardFeeRequest(urlPath) -> {
                val params = parseHttpRequest(request)
                val creditCardId = params["creditCardId"]
                if (creditCardId.equals("000000")) {
                    getMockResponse("m/api/rails/trip/unknown_error.json")
                } else {
                    getMockResponse("m/api/rails/trip/cardfee/visa.json")
                }
            }

            else -> make404()
        }
    }
}

class RailApiRequestMatcher {
    companion object {
        fun isRailApiRequest(urlPath: String): Boolean {
            return urlPath.startsWith("/rails") || urlPath.startsWith("/m/api/rail")
        }

        fun isRailApiSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/shop/search.*$", urlPath) || doesItMatch("^/m/api/rail/search.*$", urlPath)
        }

        fun isRailApiCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/trip/createTrip.*$", urlPath) || doesItMatch("^/m/api/rail/trip.*$", urlPath)
        }

        fun isRailApiCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/trip/checkout.*$", urlPath) || doesItMatch("^/m/api/rail/checkout.*$", urlPath)
        }

        fun isRailApiCardsRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/static/RailCards.*$", urlPath) || doesItMatch("^/m/api/rail/railcards.*$", urlPath)
        }

        fun isRailApiCardFeeRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/trip/cardFee.*$", urlPath) || doesItMatch("^/m/api/rail/cardfee.*$", urlPath)
        }
    }
}
