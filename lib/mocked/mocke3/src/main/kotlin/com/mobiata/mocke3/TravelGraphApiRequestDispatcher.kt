package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class TravelGraphApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path

        if (!TravelGraphApiRequestMatcher.isTravelGraphRequest(path)) {
            throwUnsupportedRequestException(path)
        }

        return getMockResponse("api/travelgraph/userSearchHistory.json")
    }
}

class TravelGraphApiRequestMatcher {
    companion object {
        fun isTravelGraphRequest(path: String): Boolean = path.contains("/travelGraphUserHistory")
    }
}