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
            FlightMApiRequestMatcher.isRichContentRequest(urlPath) ->
                getMockResponse(FlightMApiMockResponseGenerator.getRichContentResponseFilePath())

            else -> make404()
        }
    }
}

class FlightMApiMockResponseGenerator {
    companion object {
        val RICH_CONTENT = "flight_rich_content"

        fun getRichContentResponseFilePath(): String {
            return "m/api/flight/richcontent/$RICH_CONTENT.json"
        }
    }
}

class FlightMApiRequestMatcher() {
    companion object {
        fun isFlightMApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/.*$", urlPath)
        }

        fun isRichContentRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/getRichContent.*$", urlPath)
        }
    }
}
