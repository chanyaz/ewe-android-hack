package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.regex.Pattern

public class HotelRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    private var createTripRequestCount = 0

    override fun dispatch(request: RecordedRequest): MockResponse {

        val urlPath = request.path
        val params = parseRequest(request)

        if (!HotelRequestMatcher.isHotelRequest(urlPath) && !HotelRequestMatcher.isCouponRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            HotelRequestMatcher.isHotelSearchRequest(urlPath) -> {
                val gaiaId = params["regionId"]
                if (gaiaId == "error_response") {
                    return getMockResponse("m/api/hotel/search/mock_error.json")
                }
                return getMockResponse("m/api/hotel/search/happy.json")
            }

            HotelRequestMatcher.isHotelInfoRequest(urlPath) -> getMockResponse("m/api/hotel/info/" + params.get("hotelId") + ".json", params)

            HotelRequestMatcher.isHotelOffersRequest(urlPath) -> getMockResponse("m/api/hotel/offers/" + params.get("hotelId") + ".json", params)

            HotelRequestMatcher.isHotelProductRequest(urlPath) -> {
                val productKey = params.get("productKey") ?: return make404()
                val isHotelCouponError = HotelRequestMatcher.doesItMatch("^hotel_coupon_errors$", productKey)
                if (isHotelCouponError) params.put("productKey", "hotel_coupon_errors")

                return getMockResponse("m/api/hotel/product/" + params.get("productKey") + ".json", params)
            }

            HotelRequestMatcher.isHotelCreateTripRequest(urlPath) -> {
                val productKey = params.get("productKey") ?: return make404()
                val isHotelCouponError = HotelRequestMatcher.doesItMatch("^hotel_coupon_errors$", productKey)
                var fileName = if (!isHotelCouponError) productKey else "hotel_coupon_errors"
                if (productKey == "tealeaf_id" && createTripRequestCount++ == 1) {
                    fileName = "tealeaf_id_signed_in"
                }
                injectDates(params)
                return getMockResponse("m/api/hotel/trip/create/$fileName.json", params)
            }

            HotelRequestMatcher.isCouponCall(urlPath) -> getMockResponse("api/m/trip/coupon/" + params.get("coupon.code") + ".json", params)

            HotelRequestMatcher.isHotelCheckoutRequest(urlPath) -> {
                val tripId = params.get("tripId") ?: throw RuntimeException("tripId required")
                val expectedTotalFare = params.get("expectedTotalFare") ?: throw RuntimeException("expectedTotalFare required")
                val tealeafTransactionId = params.get("tealeafTransactionId") ?: throw RuntimeException("tealeafTransactionId required")

                if ("tealeafHotel:" + tripId != tealeafTransactionId) {
                    throw RuntimeException("tripId must match tealeafTransactionId got: $tealeafTransactionId")
                }

                if (!HotelRequestMatcher.isExpectedFareFormatDecimal(expectedTotalFare)) {
                    throw RuntimeException("expectedTotalFare must be in decimal format")
                }

                val isHotelCouponError = HotelRequestMatcher.doesItMatch("^hotel_coupon_errors$", tripId)
                val fileName = if (!isHotelCouponError) tripId else "hotel_coupon_errors"

                return getMockResponse("m/api/hotel/trip/checkout/$fileName.json", params)
            }

            else -> make404()
        }
    }

    private fun injectDates(params: MutableMap<String, String> ) {
        var dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
        val checkIn = DateTime.now()
        val checkOut = checkIn.plusDays(2)
        params.put("checkInDate", dtf.print(checkIn))
        params.put("checkOutDate", dtf.print(checkOut))
    }
}

class HotelRequestMatcher() {
    companion object {
        fun isHotelCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/trip/checkout.*$", urlPath)
        }

        fun isCouponCall(urlPath: String): Boolean {
            return doesItMatch("^/api/m/trip/coupon.*$", urlPath)
        }

        fun isHotelCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/trip/create.*$", urlPath)
        }

        fun isHotelProductRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/product.*$", urlPath)
        }

        fun isHotelInfoRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/info.*$", urlPath)
        }

        fun isHotelOffersRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/offers.*$", urlPath)
        }

        fun isCouponRequest(urlPath: String): Boolean {
            return doesItMatch("^/api/m/trip/coupon.*$", urlPath)
        }

        fun isHotelRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/.*$", urlPath)
        }

        fun isHotelSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/search.*$", urlPath)
        }

        fun isExpectedFareFormatDecimal(number: String) : Boolean {
            return doesItMatch("\\d*\\.\\d\\d", number)
        }

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }
}
