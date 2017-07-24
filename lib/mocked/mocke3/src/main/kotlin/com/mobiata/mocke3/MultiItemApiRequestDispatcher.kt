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
            MultiItemApiRequestMatcher.isRoomsSearchRequest(urlPath) -> {
                val productKey = urlParams["hotelId"] ?: return make404()
                getMockResponse("api/multiitem/v1/$productKey.json")
            }
            MultiItemApiRequestMatcher.isFlightOutboundSearchRequest(urlPath) -> {
                val productKey = urlParams["ratePlanCode"] ?: return make404()
                getMockResponse("api/multiitem/v1/$productKey.json")
            }
            MultiItemApiRequestMatcher.isFlightInboundSearchRequest(urlPath) -> {
                val productKey = urlParams["legId[0]"] ?: return make404()
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

        fun isRoomsSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/multiitem/v1/rooms.*$", urlPath)
        }

        fun isFlightOutboundSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/multiitem/v1/flights.*legIndex=0.*$", urlPath)
        }

        fun isFlightInboundSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/multiitem/v1/flights.*legIndex=1.*$", urlPath)
        }
    }
}
