package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

abstract class AbstractCouponParameters(val tripId: String,
                                        val isFromNotSignedInToSignedIn: Boolean,
                                        private val userPreferencePointsDetails: List<UserPreferencePointsDetails>) {

    abstract class Builder<B : Builder<B>> {
        protected var tripId: String? = null
        protected var isFromNotSignedInToSignedIn: Boolean = false
        protected var userPreferencePointsDetails: List<UserPreferencePointsDetails>? = null

        @Suppress("UNCHECKED_CAST")
        fun tripId(tripId: String): B {
            this.tripId = tripId
            return this as B
        }

        @Suppress("UNCHECKED_CAST")
        fun isFromNotSignedInToSignedIn(isFromNotSignedInToSignedIn: Boolean): B {
            this.isFromNotSignedInToSignedIn = isFromNotSignedInToSignedIn
            return this as B
        }

        @Suppress("UNCHECKED_CAST")
        fun userPreferencePointsDetails(userPreferencePointsDetails: List<UserPreferencePointsDetails>): B {
            this.userPreferencePointsDetails = userPreferencePointsDetails
            return this as B
        }

        abstract fun build(): AbstractCouponParameters
    }

    open fun toQueryMap(): HashMap<String, Any> {
        val params = HashMap<String, Any>()
        params.put("tripId", tripId)
        userPreferencePointsDetails.forEachIndexed { index, pointDetails ->
            val userPreferenceProgramName = "userPreferences[$index].programName"
            val userPreferenceAmountOnCard = "userPreferences[$index].amountOnPointsCard"
            params.put(userPreferenceProgramName, pointDetails.programName)
            params.put(userPreferenceAmountOnCard, pointDetails.payableByPoints.amount.amount.toString())
        }
        return params
    }
}
