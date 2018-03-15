package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelSavedCouponParameters(
    tripId: String,
    isFromNotSignedInToSignedIn: Boolean,
    userPreferencePointsDetails: List<UserPreferencePointsDetails>,
    val instanceId: String
) : AbstractCouponParameters(tripId, isFromNotSignedInToSignedIn, userPreferencePointsDetails) {

    class Builder : AbstractCouponParameters.Builder<HotelSavedCouponParameters.Builder>() {
        private var instanceId: String? = null

        fun instanceId(instanceId: String): Builder {
            this.instanceId = instanceId
            return this
        }

        override fun build(): HotelSavedCouponParameters {
            return HotelSavedCouponParameters(tripId ?: throw IllegalArgumentException(),
                    isFromNotSignedInToSignedIn,
                    userPreferencePointsDetails ?: throw IllegalArgumentException(),
                    instanceId ?: throw IllegalArgumentException())
        }
    }

    override fun toQueryMap(): HashMap<String, Any> {
        val params = super.toQueryMap()
        params.put("coupon.instanceId", instanceId)
        return params
    }
}
