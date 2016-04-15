package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class FeedsApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path
        if (!isFeedsApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return getMockResponse("/feeds/api/feeds/happy.json")
    }

    private fun isFeedsApiRequest(urlPath: String): Boolean {
        return doesItMatch("^/feeds/api/feeds.*$", urlPath)
    }
}
