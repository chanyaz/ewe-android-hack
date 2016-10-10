package com.mobiata.mocke3

import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.google.gson.GsonBuilder
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
                val gson = GsonBuilder().create()
                val searchParams = gson.fromJson(request.body.readUtf8(), RailApiSearchModel::class.java)
                when(searchParams.clientCode) {
                    "no_search_results" -> getMockResponse("rails/v1/shopping/search/no_search_results.json")
                    "validation_error" -> getMockResponse("rails/v1/shopping/search/validation_error.json")
                    else -> getMockResponse("rails/v1/shopping/search/happy.json")
                }
            }

            RailApiRequestMatcher.isRailApiCreateTripRequest(urlPath) -> {
                getMockResponse("m/api/rails/trip/create/happy.json")
            }

            RailApiRequestMatcher.isRailApiCheckoutRequest(urlPath) -> {
                getMockResponse("m/api/rails/trip/checkout/happy.json")
            }

            RailApiRequestMatcher.isRailApiCardsRequest(urlPath) -> {
                getMockResponse("rails/v1/shopping/cards/en_GB.json")
            }

            RailApiRequestMatcher.isRailApiCardFeeRequest(urlPath) -> {
                getMockResponse("m/api/rails/trip/cardfee/visa.json")
            }

            else -> make404()
        }
    }
}

class RailApiRequestMatcher {
    companion object {
        fun isRailApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/m/api/v1/.*$", urlPath) ||
                    doesItMatch("^/m/api/rails.*$", urlPath) ||
                    doesItMatch("^/rails/domain/api/v1/.*$", urlPath)
        }

        fun isRailApiSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/m/api/v1/search.*$", urlPath)
        }

        fun isRailApiCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/rails/trip/create.*", urlPath)
        }

        fun isRailApiCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/rails/trip/checkout.*", urlPath)
        }

        fun isRailApiCardsRequest(urlPath: String): Boolean {
            return doesItMatch("^/rails/domain/api/v1/static/RailCards.*$", urlPath)
        }

        fun isRailApiCardFeeRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/rails/trip/cardFee.*$", urlPath)
        }
    }
}
