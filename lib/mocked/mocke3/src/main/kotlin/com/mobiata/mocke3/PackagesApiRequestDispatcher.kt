package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

class PackagesApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val output = ByteArrayOutputStream()
        request.body.copyTo(output)

        val urlPath = request.path
        val urlParams = parseHttpRequest(request)

        return when {
            PackageApiRequestMatcher.isHotelSearchRequest(urlParams, urlPath) -> {
                when (urlParams["ftla"]) {
                    "GGW" -> getMockResponse("getpackages/v1/noresults.json")
                    else -> getMockResponse("getpackages/v1/happy.json")
                }
            }

            PackageApiRequestMatcher.isOutboundFlightRequest(urlParams, urlPath) -> {
                val productKey = urlParams["packagePIID"] ?: return make404()
                getMockResponse("getpackages/v1/$productKey.json")
            }

            PackageApiRequestMatcher.isReturnFlightRequest(urlParams, urlPath) -> {
                val productKey = urlParams["packagePIID"] ?: return make404()
                getMockResponse("getpackages/v1/$productKey.json")
            }

            PackageApiRequestMatcher.isHotelOffers(urlPath) -> {
                val productKey = urlParams["productKey"] ?: return make404()
                getMockResponse("api/packages/hoteloffers/$productKey.json")
            }

            PackageApiRequestMatcher.isCreateTrip(urlPath) -> {
                val productKey = urlParams["productKey"] ?: return make404()
                getMockResponse("api/packages/createtrip/$productKey.json")
            }

            PackageApiRequestMatcher.isCheckout(urlPath) -> {
                val requestBody = getRequestBody(output)
                if (requestBody.contains("errorcheckoutpricechange")) {
                    getMockResponse("api/packages/checkout/checkout_price_change.json")
                } else if (requestBody.contains("errorcheckoutcard")) {
                    getMockResponse("api/packages/checkout/error_checkout_card.json")
                } else if (requestBody.contains("errorcheckoutunknown")) {
                    getMockResponse("api/packages/checkout/error_checkout_unknown.json")
                } else getMockResponse("api/packages/checkout/checkout.json")
            }

            else -> make404()
        }
    }

    private fun getRequestBody(output: ByteArrayOutputStream): String {
        val input = ByteArrayInputStream(output.toByteArray())

        val sb = StringBuilder()
        val br = BufferedReader(InputStreamReader(input))
        var line: String? = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        return sb.toString();
    }
}

class PackageApiRequestMatcher {
    companion object {
        fun isHotelSearchRequest(urlParams: Map<String, String>, urlPath: String): Boolean {
            return !isFlightSearch(urlParams) && isPackageSearch(urlPath)
        }

        fun isOutboundFlightRequest(urlParams: Map<String, String>, urlPath: String): Boolean {
            if (!isFlightSearch(urlParams)) {
                return false
            }
            val isOutboundFlight = (urlParams["selectedLegId"] == null)
            return isOutboundFlight && isPackageSearch(urlPath)
        }

        fun isReturnFlightRequest(urlParams: Map<String, String>, urlPath: String): Boolean {
            if (!isFlightSearch(urlParams)) {
                return false
            }
            val hasSelectedDepartureLegId = (urlParams["selectedLegId"] != null)
            return hasSelectedDepartureLegId && isPackageSearch(urlPath)
        }

        fun isPackageSearch(urlPath: String): Boolean {
            return doesItMatch("^/getpackages/v1.*$", urlPath)
        }

        fun isHotelOffers(urlPath: String): Boolean {
            return doesItMatch("^/api/packages/hotelOffers.*$", urlPath)
        }

        fun isCreateTrip(urlPath: String): Boolean {
            return doesItMatch("^/api/packages/createTrip.*$", urlPath)
        }

        fun isCheckout(urlPath: String): Boolean {
            return doesItMatch("^/api/packages/checkout.*$", urlPath)
        }

        fun isFlightSearch(urlParams: Map<String, String>): Boolean {
            return "flight".equals(urlParams["searchProduct"])
        }
    }

}
