package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.regex.Pattern

public class PackagesApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path
        val urlParams = parseRequest(request)

        if (!PackageApiRequestMatcher.isPackageApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            PackageApiRequestMatcher.isHotelSearchRequest(urlParams) -> {
                getMockResponse("getpackages/v1/happy.json")
            }

            PackageApiRequestMatcher.isOutboundFlightRequest(urlParams) -> {
                getMockResponse("getpackages/v1/happy_outbound_flight.json")
            }

            PackageApiRequestMatcher.isReturnFlightRequest(urlParams) -> {
                getMockResponse("getpackages/v1/happy_inbound_flight.json")
            }

            else -> make404()
        }
    }
}

class PackageApiRequestMatcher {
    companion object {
        fun isHotelSearchRequest(urlParams: Map<String, String>): Boolean {
            return !isFlightSearch(urlParams)
        }

        fun isOutboundFlightRequest(urlParams: Map<String, String>): Boolean {
            if (!isFlightSearch(urlParams)) {
                return false
            }
            val isOutboundFlight = (urlParams["selectedLegId"] == null)
            return isOutboundFlight
        }

        fun isReturnFlightRequest(urlParams: Map<String, String>): Boolean {
            if (!isFlightSearch(urlParams)) {
                return false
            }
            val isSelectReturnFlightRequest = (urlParams["selectLegId"] as Int == 1)
            val hasSelectedDepartureLegId = (urlParams["selectedLegId"] != null)
            return isSelectReturnFlightRequest && hasSelectedDepartureLegId
        }

        fun isPackageApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/getpackages/v1.*$", urlPath)
        }

        fun isFlightSearch(urlParams: Map<String, String>): Boolean {
            return "flight".equals(urlParams["searchProduct"])
        }

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }

}
