package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money
import java.math.BigDecimal

data class LoyaltyInformation(
        val burn: LoyaltyBurnInfo?, val earn: LoyaltyEarnInfo, val isBurnApplied: Boolean
)

data class LoyaltyBurnInfo(val type: LoyaltyType, val amount: Money)

data class LoyaltyEarnInfo(val points: PointsEarnInfo?, val price: PriceEarnInfo?) {
        fun getEarnMessagePointsOrPrice(): String {
                var earnMessage = ""
                if (points?.total ?: 0 > 0) {
                        earnMessage = points?.total?.toString() ?: ""
                }

                // Earn Price
                else if (price?.total?.amount ?: BigDecimal(0) > BigDecimal(0)) {
                        earnMessage = price?.total?.formattedMoney ?: ""
                }
                return earnMessage
        }
}

// Earn information for Expedia is in points.
data class PointsEarnInfo(val base: Int, val bonus: Int, val total: Int)

// Earn information in Orbitz is in dollars.
data class PriceEarnInfo(val base: Money, val bonus: Money, val total: Money)
