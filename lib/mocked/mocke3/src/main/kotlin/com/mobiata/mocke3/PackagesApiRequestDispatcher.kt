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
                getMockResponse("getpackages/v1/happy_outbound_flight.json")
            }

            PackageApiRequestMatcher.isReturnFlightRequest(urlParams, urlPath) -> {
                getMockResponse("getpackages/v1/happy_inbound_flight.json")
            }

            PackageApiRequestMatcher.isHotelOffers(urlPath) -> {
                getMockResponse("api/packages/hoteloffers/offers.json")
            }

            PackageApiRequestMatcher.isCreateTrip(urlPath) -> {
                getMockResponse("api/packages/createtrip/create_trip.json")
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
            val isSelectReturnFlightRequest = Strings.equals(urlParams["selectLegId"], "1")
            val hasSelectedDepartureLegId = (urlParams["selectedLegId"] != null)
            return isSelectReturnFlightRequest && hasSelectedDepartureLegId && isPackageSearch(urlPath)
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
