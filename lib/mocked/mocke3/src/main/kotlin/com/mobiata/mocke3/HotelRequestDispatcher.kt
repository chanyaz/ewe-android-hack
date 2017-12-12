package com.mobiata.mocke3

import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.google.gson.GsonBuilder
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.LinkedHashMap

class HotelRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    private var createTripRequestCount = 0

    override fun dispatch(request: RecordedRequest): MockResponse {

        val urlPath = request.path
        var params: MutableMap<String, String> = LinkedHashMap()

        // Hotel checkout V2 uses JSON POST body which other requests do not.
        if (!HotelRequestMatcher.isHotelCheckoutV2Request(urlPath))
            params = parseHttpRequest(request)

        if (!HotelRequestMatcher.isHotelRequest(urlPath) && !HotelRequestMatcher.isCouponRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            HotelRequestMatcher.isHotelSearchRequest(urlPath) -> {
                val gaiaId = if (params["regionId"].isNullOrBlank()) "happy" else params["regionId"]
                return getMockResponse("m/api/hotel/search/$gaiaId.json")
            }

            HotelRequestMatcher.isHotelInfoRequest(urlPath) -> getMockResponse("m/api/hotel/info/" + params["hotelId"] + ".json", params)

            HotelRequestMatcher.isHotelOffersRequest(urlPath) -> getMockResponse("m/api/hotel/offers/" + params["hotelId"] + ".json", params)

            HotelRequestMatcher.isHotelProductRequest(urlPath) -> {
                val productKey = params["productKey"] ?: return make404()
                val isHotelCouponError = doesItMatch("^hotel_coupon_errors$", productKey)
                if (isHotelCouponError) params.put("productKey", "hotel_coupon_errors")

                return getMockResponse("m/api/hotel/product/" + params["productKey"] + ".json", params)
            }

            HotelRequestMatcher.isHotelCreateTripRequest(urlPath) -> {
                val productKey = params["productKey"] ?: return make404()
                val isHotelCouponError = doesItMatch("^hotel_coupon_errors$", productKey)
                var fileName = if (!isHotelCouponError) productKey else "hotel_coupon_errors"
                if (productKey == "tealeaf_id" && createTripRequestCount++ == 1) {
                    fileName = "tealeaf_id_signed_in"
                }
                injectDates(params)
                return getMockResponse("m/api/hotel/trip/create/$fileName.json", params)
            }

            HotelRequestMatcher.isCouponApplyCall(urlPath) -> getMockResponse("api/m/trip/coupon/" + (params["coupon.instanceId"] ?: params["coupon.code"]) + ".json", params)

            HotelRequestMatcher.isCouponRemoveCall(urlPath) -> getMockResponse("api/m/trip/remove/coupon/" + params["tripId"] + ".json", params)

            HotelRequestMatcher.isHotelCheckoutRequest(urlPath) -> {
                val tripId = params["tripId"] ?: throw RuntimeException("tripId required")
                val expectedTotalFare = params["expectedTotalFare"] ?: throw RuntimeException("expectedTotalFare required")
                val tealeafTransactionId = params["tealeafTransactionId"] ?: throw RuntimeException("tealeafTransactionId required")

                if ("tealeafHotel:" + tripId != tealeafTransactionId) {
                    throw RuntimeException("tripId must match tealeafTransactionId got: $tealeafTransactionId")
                }

                if (!HotelRequestMatcher.isExpectedFareFormatDecimal(expectedTotalFare)) {
                    throw RuntimeException("expectedTotalFare must be in decimal format")
                }

                val isHotelCouponError = doesItMatch("^hotel_coupon_errors$", tripId)
                val fileName = if (!isHotelCouponError) tripId else "hotel_coupon_errors"

                return getMockResponse("m/api/hotel/trip/checkout/$fileName.json", params)
            }


            HotelRequestMatcher.isHotelCheckoutV2Request(urlPath) -> {
                val gson = GsonBuilder().create()
                val checkoutParams = gson.fromJson(request.body.readUtf8(), HotelCheckoutV2Params::class.java)
                val tripId = checkoutParams.tripDetails.tripId
                val expectedTotalFare = checkoutParams.tripDetails.expectedTotalFare
                val tealeafTransactionId = checkoutParams.misc.teaLeafTransactionId

                if ("tealeafHotel:" + tripId != tealeafTransactionId) {
                    throw RuntimeException("tripId must match tealeafTransactionId got: $tealeafTransactionId")
                }

                if (!HotelRequestMatcher.isExpectedFareFormatDecimal(expectedTotalFare)) {
                    throw RuntimeException("expectedTotalFare must be in decimal format")
                }

                val isHotelCouponError = doesItMatch("^hotel_coupon_errors$", tripId)
                val fileName = if (!isHotelCouponError) tripId else "hotel_coupon_errors"

                return getMockResponse("m/api/hotel/trip/checkout/$fileName.json", params)
            }

            else -> make404()
        }
    }

    private fun injectDates(params: MutableMap<String, String> ) {
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
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

        fun isHotelCheckoutV2Request(urlPath: String): Boolean {
            return doesItMatch("^/m/api/hotel/trip/V2/checkout.*$", urlPath)
        }

        fun isCouponApplyCall(urlPath: String): Boolean {
            return doesItMatch("^/api/m/trip/coupon.*$", urlPath)
        }

        fun isCouponRemoveCall(urlPath: String): Boolean {
            return doesItMatch("^/api/m/trip/remove/coupon.*$", urlPath)
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
            return isCouponApplyCall(urlPath) || isCouponRemoveCall(urlPath)
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
    }
}
