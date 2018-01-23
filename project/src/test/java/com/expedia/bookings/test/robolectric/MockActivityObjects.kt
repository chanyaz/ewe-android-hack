package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.mobiata.mocke3.mockObject

class MockActivityObjects {

    private lateinit var lxCheckoutParams: LXCheckoutParams

    fun getHappyOffersResponse(): ActivityDetailsResponse {
        return getActivityOffersResponse("happy")
    }

    private fun getActivityOffersResponse(activityId: String): ActivityDetailsResponse {
        return mockObject(ActivityDetailsResponse::class.java, "lx/api/activity/$activityId.json", null)!!
    }

    fun getCheckoutError(errorType: String): ApiError {
        val response = lxCheckoutResponse(errorType)
        return response.errors.first()
    }

    private fun mapErrorToFilename(errorType: String): String {
        return when (errorType) {
            "AlreadyBooked" -> "m/api/lx/trip/checkout/trip_already_booked.json"
            "PaymentFailed" -> "m/api/lx/trip/checkout/payment_failed_trip_id.json"
            "UnknownError" -> "m/api/lx/trip/checkout/unknown_error.json"
            "SessionTimeout" -> "m/api/lx/trip/checkout/session_timeout.json"
            "InvalidInput" -> "m/api/lx/trip/checkout/invalid_input.json"
            "PriceChange" -> "m/api/lx/trip/checkout/price_change.json"
            else -> "m/api/lx/trip/checkout/$errorType.json"
        }
    }

    fun getCheckoutResponseForPriceChange(): LXCheckoutResponse {
        return lxCheckoutResponse("PriceChange")
    }

    private fun lxCheckoutResponse(errorType: String): LXCheckoutResponse {
        setCheckoutParamsWithErrorAsFirstName(errorType)
        val fileName = mapErrorToFilename(errorType)
        val checkoutResponse = mockObject(LXCheckoutResponse::class.java, fileName, null)!!
        checkoutResponse.originalPrice = Money("100", "USD")
        return checkoutResponse
    }

    fun setCheckoutParamsWithErrorAsFirstName(errorType: String, isRequiredCVV: Boolean = true) {
        lxCheckoutParams = LXCheckoutParams()
        lxCheckoutParams.firstName = errorType
        lxCheckoutParams.lastName = "Test"
        if (isRequiredCVV)
            lxCheckoutParams.cvv = "111"
        lxCheckoutParams.expectedTotalFare = "180.00"
        lxCheckoutParams.phone = "456-4567"
        lxCheckoutParams.email = "qa-ehcc@mobiata.com"
        lxCheckoutParams.tripId = "happypath_trip_id"
        lxCheckoutParams.expectedFareCurrencyCode = "USD"
        lxCheckoutParams.phoneCountryCode = "1"
    }

    fun getCheckoutParams(): LXCheckoutParams {
        return lxCheckoutParams
    }
}
