package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import java.util.HashMap

class HotelRemoveCouponParameters(val tripId: String, val userPreferencePointsDetails: List<UserPreferencePointsDetails>) {

    class Builder {
        private var tripId: String? = null
        private var userPreferencePointsDetails: List<UserPreferencePointsDetails>? = null

        fun tripId(tripId: String): HotelRemoveCouponParameters.Builder {
            this.tripId = tripId
            return this
        }

        fun userPreferencePointsDetails(userPreferencePointsDetails: List<UserPreferencePointsDetails>): HotelRemoveCouponParameters.Builder {
            this.userPreferencePointsDetails = userPreferencePointsDetails
            return this
        }

        fun build(): HotelRemoveCouponParameters {
            return HotelRemoveCouponParameters(tripId?: throw IllegalArgumentException(),
                    userPreferencePointsDetails?: throw IllegalArgumentException())
        }
    }

    fun toQueryMap(): Map<String, Any> {
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