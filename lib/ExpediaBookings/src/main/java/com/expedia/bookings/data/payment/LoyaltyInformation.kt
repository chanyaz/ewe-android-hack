package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money
import java.math.BigDecimal

enum class LoyaltyEarnInfoType {
        NONE,
        POINTS,
        MONEY
}

data class LoyaltyInformation(
        val burn: LoyaltyBurnInfo?, val earn: LoyaltyEarnInfo, val isBurnApplied: Boolean
)

data class LoyaltyBurnInfo(val type: LoyaltyType, val amount: Money)

data class LoyaltyEarnInfo(val points: PointsEarnInfo?, val price: PriceEarnInfo?) {

        fun loyaltyEarnInfoType(): LoyaltyEarnInfoType {
                if (points?.total != null) {
                        return LoyaltyEarnInfoType.POINTS
                } else if (price?.total?.amount != null) {
                        return LoyaltyEarnInfoType.MONEY
                } else {
                        return LoyaltyEarnInfoType.NONE
                }
        }


        fun getEarnMessagePointsOrPriceWithNonZeroValue(): String {
                when (loyaltyEarnInfoType()) {
                        LoyaltyEarnInfoType.POINTS -> {
                                if (points?.total!! > 0) return points?.total.toString() else return ""
                        }
                        LoyaltyEarnInfoType.MONEY -> {
                                if (price?.total?.getAmount()!! > BigDecimal.ZERO) return price?.total?.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)!! else return ""
                        }
                        LoyaltyEarnInfoType.NONE -> return ""
                }
        }
}

// Earn information for Expedia is in points.
data class PointsEarnInfo(val base: Int, val bonus: Int, val total: Int)

// Earn information in Orbitz is in dollars.
data class PriceEarnInfo(val base: Money?, val bonus: Money?, val total: Money)
