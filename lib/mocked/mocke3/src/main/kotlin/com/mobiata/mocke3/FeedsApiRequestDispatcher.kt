package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest

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
