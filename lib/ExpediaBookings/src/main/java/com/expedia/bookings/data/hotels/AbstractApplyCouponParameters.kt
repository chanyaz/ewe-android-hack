package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

abstract class AbstractApplyCouponParameters(val tripId: String,
                                             val isFromNotSignedInToSignedIn: Boolean,
                                             val userPreferencePointsDetails: List<UserPreferencePointsDetails>) {

    abstract class Builder<B : Builder<B>> {
        protected var tripId: String? = null
        protected var isFromNotSignedInToSignedIn: Boolean = false
        protected var userPreferencePointsDetails: List<UserPreferencePointsDetails>? = null

        fun tripId(tripId: String): B {
            this.tripId = tripId
            return this as B
        }

        fun isFromNotSignedInToSignedIn(isFromNotSignedInToSignedIn: Boolean): B {
            this.isFromNotSignedInToSignedIn = isFromNotSignedInToSignedIn
            return this as B
        }

        fun userPreferencePointsDetails(userPreferencePointsDetails: List<UserPreferencePointsDetails>): B {
            this.userPreferencePointsDetails = userPreferencePointsDetails
            return this as B
        }

        abstract fun build(): AbstractApplyCouponParameters
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

    abstract fun getTrackingString(): String
}