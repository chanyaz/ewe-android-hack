package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.ByteArrayOutputStream

class MultiItemApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val output = ByteArrayOutputStream()
        request.body.copyTo(output)

        val urlPath = request.path
        val urlParams = parseHttpRequest(request)

        return when {
            MultiItemApiRequestMatcher.isHotelSearchRequest(urlPath) -> {
                val productKey = urlParams["origin"] ?: return make404()
                getMockResponse("api/multiitem/v1/$productKey.json")
            }
            else -> make404()
        }
    }
}

class MultiItemApiRequestMatcher {
    companion object {
        fun isHotelSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/multiitem/v1/hotels.*$", urlPath)
        }
    }
}
