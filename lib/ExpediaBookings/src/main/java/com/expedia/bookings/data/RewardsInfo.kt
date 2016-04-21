package com.expedia.bookings.data

import kotlin.properties.Delegates

class RewardsInfo {
    val totalPointsToEarn: Float = 0f
    var totalAmountToEarn: Money? = null
    val isActiveRewardsMember: Boolean = false
    val rewardsMembershipTierName: String by Delegates.notNull()
    //Utility Member for local modifications in case we receive updated expedia rewards when we modify the Points to be burned. Not received by deserialization/server-response.
    private var updatedRewards: Float? = null

    fun setUpdatedRewards(points: Float) {
        updatedRewards = points
    }

    fun getUpdatedRewards(): Float? {
        return if (updatedRewards != null) updatedRewards else totalPointsToEarn
    }
}