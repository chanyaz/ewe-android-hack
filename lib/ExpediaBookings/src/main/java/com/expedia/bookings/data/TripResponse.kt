package com.expedia.bookings.data

import com.expedia.bookings.data.cars.BaseApiResponse
import com.expedia.bookings.data.payment.PointsDetails
import com.expedia.bookings.data.payment.PointsProgramType
import kotlin.collections.forEach
import kotlin.properties.Delegates

public abstract class TripResponse : BaseApiResponse() {
    lateinit var tripId: String
    var pointsDetails: List<PointsDetails>? = null
    lateinit var validFormsOfPayment: List<ValidPayment>
    lateinit var expediaRewards: ExpediaRewards

    class ExpediaRewards {
        val totalPointsToEarn: Int = 0
        val isActiveRewardsMember: Boolean = false
        val rewardsMembershipTierName: String by Delegates.notNull()
    }

    fun getPointDetails(pointsProgramType: PointsProgramType): PointsDetails? {
        pointsDetails?.forEach {
            if (it.programName == pointsProgramType) {
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