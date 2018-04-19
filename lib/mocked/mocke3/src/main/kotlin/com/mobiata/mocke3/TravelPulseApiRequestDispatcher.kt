package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class TravelPulseApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path

        return when {
            TravelPulseApiRequestMatcher.isTravelPulseFetchRequest(path) ->
                getMockResponse("api/travelpulse/fetchResponse.json")
            else ->
                make404()
        }
    }
}

class TravelPulseApiRequestMatcher {
    companion object {
        fun isTravelPulseRequest(urlPath: String): Boolean  {
            return urlPath.startsWith("/service/shortlist/")
        }

        fun isTravelPulseFetchRequest(urlPath: String): Boolean {
            return urlPath.startsWith("/service/shortlist/detail/fetch/")
        }
    }
}
