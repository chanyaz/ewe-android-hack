package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import java.util.regex.Matcher
import java.util.regex.Pattern

public class HotelRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener: FileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {

        val urlPath = request.getPath()
        val params = parseRequest(request)

        if (!HotelRequestMatcher.isHotelRequest(urlPath) && !HotelRequestMatcher.isCouponRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            HotelRequestMatcher.isHotelSearchRequest(urlPath) -> getMockResponse("m/api/hotel/search/happy.json")

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
                val fileName = if (!isHotelCouponError) productKey else "hotel_coupon_errors"

                return getMockResponse("m/api/hotel/trip/create/" + fileName + ".json", params)
            }

            HotelRequestMatcher.isCouponCall(urlPath) -> getMockResponse("api/m/trip/coupon/" + params.get("coupon.code") + ".json", params)

            HotelRequestMatcher.isHotelCheckoutRequest(urlPath) -> {
                val tripId = params.get("tripId") ?: return make404()
                val isHotelCouponError = HotelRequestMatcher.doesItMatch("^hotel_coupon_errors$", tripId)
                val fileName = if (!isHotelCouponError) tripId else "hotel_coupon_errors"

                return getMockResponse("m/api/hotel/trip/checkout/" + fileName + ".json", params)
            }

            else -> make404()
        }
    }
}

class HotelRequestMatcher() {
    companion object {
        fun isHotelCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/trip/checkout.*$", urlPath)
        }

        fun isCouponCall(urlPath: String): Boolean {
            return doesItMatch("^/api/m/trip/coupon$", urlPath)
        }

        fun isHotelCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/trip/create.*$", urlPath)
        }

        fun isHotelProductRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/product.*$", urlPath)
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

        fun doesItMatch(regExp: String, str: String): Boolean {
            val pattern = Pattern.compile(regExp)
            val matcher = pattern.matcher(str)
            return matcher.matches()
        }
    }
}
