package com.mobiata.mocke3

import com.expedia.bookings.utils.Strings
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.regex.Pattern
import kotlin.text.toInt

public class PackagesApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path
        val urlParams = parseRequest(request)

        return when {
            PackageApiRequestMatcher.isHotelSearchRequest(urlParams, urlPath) -> {
                getMockResponse("getpackages/v1/happy.json")
            }

            PackageApiRequestMatcher.isOutboundFlightRequest(urlParams, urlPath) -> {
                val productKey = urlParams.get("packagePIID") ?: return make404()
                getMockResponse("getpackages/v1/$productKey.json")
            }

            PackageApiRequestMatcher.isReturnFlightRequest(urlParams, urlPath) -> {
                val productKey = urlParams.get("packagePIID") ?: return make404()
                getMockResponse("getpackages/v1/$productKey.json")
            }

            PackageApiRequestMatcher.isHotelOffers(urlPath) -> {
                val productKey = urlParams.get("productKey") ?: return make404()
                getMockResponse("api/packages/hoteloffers/$productKey.json")
            }

            PackageApiRequestMatcher.isCreateTrip(urlPath) -> {
                val productKey = urlParams.get("productKey") ?: return make404()
                getMockResponse("api/packages/createtrip/$productKey.json")
            }

            PackageApiRequestMatcher.isCheckout(urlPath) -> {
                getMockResponse("api/packages/checkout/checkout.json")
            }

            else -> make404()
        }
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

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }

}
