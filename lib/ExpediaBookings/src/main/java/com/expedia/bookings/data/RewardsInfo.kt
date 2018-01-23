package com.expedia.bookings.data

import com.expedia.bookings.data.payment.PointsAndCurrency

class RewardsInfo {
    var totalPointsToEarn: Float = 0f
    var totalAmountToEarn: Money? = null

    //Utility Member for local modifications in case we receive updated expedia rewards when we modify the Points to be burned. Not received by deserialization/server-response.
    private var updatedPointsAndCurrencyToEarn: PointsAndCurrency? = null

    fun updatePointsAndCurrencyToEarn(pointsAndCurrency: PointsAndCurrency) {
        updatedPointsAndCurrencyToEarn = pointsAndCurrency
    }

    fun getPointsToEarn(): Float? {
        return updatedPointsAndCurrencyToEarn?.points ?: totalPointsToEarn
    }

    fun getAmountToEarn(): Money? {
        return updatedPointsAndCurrencyToEarn?.amountToEarn ?: totalAmountToEarn
    }

    fun hasAmountToEarn(): Boolean {
        return totalAmountToEarn != null && !totalAmountToEarn!!.isZero
    }

    fun hasPointsToEarn(): Boolean {
        return totalPointsToEarn != 0f
    }
}
