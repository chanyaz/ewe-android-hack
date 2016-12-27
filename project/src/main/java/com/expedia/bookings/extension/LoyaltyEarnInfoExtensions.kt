package com.expedia.bookings.extension

import com.expedia.bookings.R
import android.content.Context
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyEarnInfoType
import com.squareup.phrase.Phrase

fun LoyaltyEarnInfo.getEarnMessage(context: Context): String {
    when (this.loyaltyEarnInfoType()) {
        LoyaltyEarnInfoType.MONEY ->
            return if (!getEarnMessagePointsOrPriceWithNonZeroValue().isEmpty()) return Phrase.from(context.getString(R.string.earn_amount_TEMPLATE)).put("price", getEarnMessagePointsOrPriceWithNonZeroValue()).format().toString() else return ""
        LoyaltyEarnInfoType.POINTS ->
            return if (!getEarnMessagePointsOrPriceWithNonZeroValue().isEmpty()) return Phrase.from(context.getString(R.string.earn_points_TEMPLATE)).put("points", getEarnMessagePointsOrPriceWithNonZeroValue()).format().toString() else return ""
        LoyaltyEarnInfoType.NONE ->
            return ""
    }
}
