package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelApplyCouponParameters(
    tripId: String,
    isFromNotSignedInToSignedIn: Boolean,
    userPreferencePointsDetails: List<UserPreferencePointsDetails>,
    val couponCode: String
) : AbstractCouponParameters(tripId, isFromNotSignedInToSignedIn, userPreferencePointsDetails) {

    class Builder : AbstractCouponParameters.Builder<Builder>() {
        private var couponCode: String? = null

        fun couponCode(couponCode: String): Builder {
            this.couponCode = couponCode
            return this
        }

        override fun build(): HotelApplyCouponParameters {
            return HotelApplyCouponParameters(tripId ?: throw IllegalArgumentException(),
                    isFromNotSignedInToSignedIn,
                    userPreferencePointsDetails ?: throw IllegalArgumentException(),
                    couponCode ?: throw IllegalArgumentException())
        }
    }

    override fun toQueryMap(): HashMap<String, Any> {
        val params = super.toQueryMap()
        params.put("coupon.code", couponCode)
        return params
    }
}
