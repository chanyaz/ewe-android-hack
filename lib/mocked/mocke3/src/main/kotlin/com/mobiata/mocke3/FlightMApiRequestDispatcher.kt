package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class FlightMApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!FlightMApiRequestMatcher.isFlightMApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            FlightMApiRequestMatcher.isRouteHappyRequest(urlPath) ->
                getMockResponse(FlightMApiMockResponseGenerator.getRouteHappyResponseFilePath())

            else -> make404()
        }
    }
}

class FlightMApiMockResponseGenerator {
    companion object {
        val ROUTE_HAPPY = "flight_route_happy"

        fun getRouteHappyResponseFilePath(): String {
            return "m/api/flight/routehappy/$ROUTE_HAPPY.json"
        }
    }
}

class FlightMApiRequestMatcher() {
    companion object {
        fun isFlightMApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/.*$", urlPath)
        }

        fun isRouteHappyRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/getRichContent.*$", urlPath)
        }
    }
}
