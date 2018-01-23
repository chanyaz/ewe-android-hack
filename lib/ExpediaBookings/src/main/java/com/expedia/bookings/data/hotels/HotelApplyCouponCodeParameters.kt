package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.hotels.AbstractApplyCouponParameters.Builder
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelApplyCouponCodeParameters(tripId: String,
                                     isFromNotSignedInToSignedIn: Boolean,
                                     userPreferencePointsDetails: List<UserPreferencePointsDetails>,
                                     val couponCode: String) : AbstractApplyCouponParameters(tripId, isFromNotSignedInToSignedIn, userPreferencePointsDetails) {

    class Builder : AbstractApplyCouponParameters.Builder<Builder>() {
        private var couponCode: String? = null

        fun couponCode(couponCode: String): Builder {
            this.couponCode = couponCode
            return this
        }

        override fun build(): HotelApplyCouponCodeParameters {
            return HotelApplyCouponCodeParameters(tripId ?: throw IllegalArgumentException(),
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

    override fun getTrackingString(): String {
        return couponCode
    }
}
