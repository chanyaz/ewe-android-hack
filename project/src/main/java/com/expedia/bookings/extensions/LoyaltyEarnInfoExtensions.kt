package com.expedia.bookings.extensions

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyEarnInfoType
import com.squareup.phrase.Phrase

fun LoyaltyEarnInfo.getEarnMessage(context: Context, showCurrency: Boolean): String {

    if (getEarnMessagePointsOrPriceWithNonZeroValue().isEmpty()) return "" else {
        when (this.loyaltyEarnInfoType()) {
            LoyaltyEarnInfoType.MONEY ->
                return when {
                    (!showCurrency) -> Phrase.from(context.getString(R.string.earn_amount_TEMPLATE)).put("price", getEarnMessagePointsOrPriceWithNonZeroValue()).format().toString()
                    else -> Phrase.from(context.getString(R.string.earn_amount_flight_TEMPLATE)).put("price", getEarnMessagePointsOrPriceWithNonZeroValue()).put("currency", context.getString(R.string.brand_reward_currency)).format().toString()
                }

            LoyaltyEarnInfoType.POINTS ->
                return Phrase.from(context.getString(R.string.earn_points_TEMPLATE)).put("points", getEarnMessagePointsOrPriceWithNonZeroValue()).format().toString()
            LoyaltyEarnInfoType.NONE ->
                return ""
        }
    }
}

fun LoyaltyEarnInfo.getEarnMessage(context: Context): String {
    return getEarnMessage(context, false)
}
