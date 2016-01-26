package com.expedia.bookings.data

import com.expedia.bookings.data.cars.BaseApiResponse
import com.expedia.bookings.data.payment.PointsDetails
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPaymentPreferences
import kotlin.collections.forEach
import kotlin.properties.Delegates

public abstract class TripResponse : BaseApiResponse() {
    lateinit var tripId: String
    var pointsDetails: List<PointsDetails>? = null
    lateinit var userPreferencePoints: UserPaymentPreferences
    lateinit var validFormsOfPayment: List<ValidPayment>
    lateinit var expediaRewards: ExpediaRewards
    var guestUserPromoEmailOptInStatus : String? = null

    class ExpediaRewards {
        var totalPointsToEarn: Int = 0
        val isActiveRewardsMember: Boolean = false
        val rewardsMembershipTierName: String by Delegates.notNull()
    }

    fun getPointDetails(programName: ProgramName): PointsDetails? {
        pointsDetails?.forEach {
            if (it.programName == programName) {
                return it
            }
        }
        return null
    }

    fun isExpediaRewardsRedeemable(): Boolean {
        // Pay With Points is not allowed in 6.7
        return false
    }

    abstract fun getTripTotal(): Money
}