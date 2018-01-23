package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelApplySavedCodeParameters(tripId: String,
                                    isFromNotSignedInToSignedIn: Boolean,
                                    userPreferencePointsDetails: List<UserPreferencePointsDetails>,
                                    val instanceId: String) : AbstractApplyCouponParameters(tripId, isFromNotSignedInToSignedIn, userPreferencePointsDetails) {

    class Builder : AbstractApplyCouponParameters.Builder<HotelApplySavedCodeParameters.Builder>() {
        private var instanceId: String? = null

        fun instanceId(instanceId: String): Builder {
            this.instanceId = instanceId
            return this
        }

        override fun build(): HotelApplySavedCodeParameters {
            return HotelApplySavedCodeParameters(tripId ?: throw IllegalArgumentException(),
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

    override fun getTrackingString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
