package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class SatelliteApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!SatelliteApiRequestMatcher.isSatelliteApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            SatelliteApiRequestMatcher.isSatelliteApiRequest(urlPath) -> {
                getMockResponse("m/api/satellite/featureConfig.json")
            }
            else -> make404()
        }
    }
}

class SatelliteApiRequestMatcher {
    companion object {
        fun isSatelliteApiRequest(urlPath: String): Boolean {
            return urlPath.startsWith("/m/api/config/feature")
        }
    }
}
