package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class RailApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!RailApiRequestMatcher.isRailApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            RailApiRequestMatcher.isRailApiSearchRequest(urlPath) -> {
                getMockResponse("rails/v1/shopping/search/happy.json")
            }

            RailApiRequestMatcher.isRailApiDetailsRequest(urlPath) -> {
                getMockResponse("rails/v1/shopping/getDetails/happy.json")
            }

            RailApiRequestMatcher.isRailApiValidateOfferRequest(urlPath) -> {
                getMockResponse("rails/v1/shopping/validateOffer/happy.json")
            }

            RailApiRequestMatcher.isRailApiCreateTripRequest(urlPath) -> {
                getMockResponse("m/api/rails/trip/create/happy.json")
            }

            RailApiRequestMatcher.isRailApiCheckoutRequest(urlPath) -> {
                getMockResponse("m/api/rails/trip/checkout/happy.json")
            }

            else -> make404()
        }
    }
}

class RailApiRequestMatcher {
    companion object {
        fun isRailApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/ecom/v1/shopping/.*$", urlPath) || doesItMatch("^/m/api/rails.*$", urlPath)
        }

        fun isRailApiSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/ecom/v1/shopping/search.*$", urlPath)
        }

        fun isRailApiDetailsRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/ecom/v1/shopping/getDetails.*$", urlPath)
        }

        fun isRailApiValidateOfferRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/ecom/v1/shopping/validateOffer.*$", urlPath)
        }

        fun isRailApiCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/rails/trip/create.*", urlPath)
        }

        fun isRailApiCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/rails/trip/checkout.*", urlPath)
        }

    }
}
