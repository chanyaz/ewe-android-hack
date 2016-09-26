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
                getMockResponse("rails/shop/search/happy.json")
            }

            RailApiRequestMatcher.isRailApiCreateTripRequest(urlPath) -> {
                getMockResponse("/rails/trip/create/happy.json")
            }

            RailApiRequestMatcher.isRailApiCheckoutRequest(urlPath) -> {
                getMockResponse("rails/trip/checkout/happy.json")
            }

            RailApiRequestMatcher.isRailApiCardsRequest(urlPath) -> {
                getMockResponse("rails/v1/shopping/cards/en_GB.json")
            }

            else -> make404()
        }
    }
}

class RailApiRequestMatcher {
    companion object {
        fun isRailApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails.*$", urlPath) || doesItMatch("^/shop.*$", urlPath) || doesItMatch("^/trip.*$", urlPath)
        }

        fun isRailApiSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/shop/search.*$", urlPath)
        }

        fun isRailApiCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/trip/create.*", urlPath)
        }

        fun isRailApiCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/trip/checkout.*", urlPath)
        }

        fun isRailApiCardsRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/api/v1/static/RailCards.*$", urlPath)
        }

    }
}
