package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelApplyCouponParameters(val tripId: String, val couponCode: String,
                                val isFromNotSignedInToSignedIn: Boolean,
                                val userPreferencePointsDetails: List<UserPreferencePointsDetails>) {

    class Builder {
        private var tripId: String? = null
        private var couponCode: String? = null
        private var isFromNotSignedInToSignedIn: Boolean = false
        private var userPreferencePointsDetails: List<UserPreferencePointsDetails>? = null

        fun tripId(tripId: String): HotelApplyCouponParameters.Builder {
            this.tripId = tripId
            return this
        }

        fun couponCode(couponCode: String): HotelApplyCouponParameters.Builder {
            this.couponCode = couponCode
            return this
        }

        fun isFromNotSignedInToSignedIn(isFromNotSignedInToSignedIn: Boolean): HotelApplyCouponParameters.Builder {
            this.isFromNotSignedInToSignedIn = isFromNotSignedInToSignedIn
            return this
        }

        fun userPreferencePointsDetails(userPreferencePointsDetails: List<UserPreferencePointsDetails>): HotelApplyCouponParameters.Builder {
            this.userPreferencePointsDetails = userPreferencePointsDetails
            return this
        }

        fun build(): HotelApplyCouponParameters {
            return HotelApplyCouponParameters(tripId?: throw IllegalArgumentException(),
                    couponCode?: throw IllegalArgumentException(),
                    isFromNotSignedInToSignedIn,
                    userPreferencePointsDetails?: throw IllegalArgumentException()
            )
        }
    }

    fun toQueryMap(): Map<String, Any> {
        val params = HashMap<String, Any>()
        params.put("tripId", tripId)
        params.put("coupon.code", couponCode)
        userPreferencePointsDetails.forEachIndexed { index, pointDetails ->
            val userPreferenceProgramName = "userPreferences[$index].programName"
            val userPreferenceAmountOnCard = "userPreferences[$index].amountOnPointsCard"
            params.put(userPreferenceProgramName, pointDetails.programName)
            params.put(userPreferenceAmountOnCard, pointDetails.payableByPoints.amount.amount.toString())
        }
        return params
    }
}