package com.expedia.bookings.extension

import com.expedia.bookings.R
import android.content.Context
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyEarnInfoType
import com.squareup.phrase.Phrase

fun LoyaltyEarnInfo.getEarnMessage(context: Context): String {
    when (this.loyaltyEarnInfoType()) {
        LoyaltyEarnInfoType.MONEY ->
            return Phrase.from(context.getString(R.string.earn_amount_TEMPLATE)).put("price", getEarnMessagePointsOrPrice()).format().toString()
        LoyaltyEarnInfoType.POINTS ->
            return Phrase.from(context.getString(R.string.earn_points_TEMPLATE)).put("points", getEarnMessagePointsOrPrice()).format().toString()
        LoyaltyEarnInfoType.NONE ->
            return ""
    }
}
