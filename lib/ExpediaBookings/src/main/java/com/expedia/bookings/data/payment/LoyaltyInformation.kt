package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money

data class LoyaltyInformation(
        val burn: LoyaltyBurnInfo?, val earn: LoyaltyEarnInfo, val isShopWithPoints: Boolean
)

data class LoyaltyBurnInfo(val type: LoyaltyType, val amount: Money)

data class LoyaltyEarnInfo(val points: PointsEarnInfo?, val price: PriceEarnInfo?)

// Earn information for Expedia is in points.
data class PointsEarnInfo(val base: Int, val bonus: Int, val total: Int)

// Earn information in Orbitz is in dollars.
data class PriceEarnInfo(val base: Money, val bonus: Money, val total: Money)