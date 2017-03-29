package com.expedia.bookings.data

import com.expedia.bookings.data.payment.PointsAndCurrency
import kotlin.properties.Delegates

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
}