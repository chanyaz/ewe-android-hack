package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.squareup.phrase.Phrase

fun getEarnMessage(context: Context, earn: LoyaltyEarnInfo?): String {
    val earnMessagePointsOrPrice = earn?.getEarnMessagePointsOrPrice()
    if (earnMessagePointsOrPrice?.isNotBlank() ?: false) {
        return Phrase.from(context.resources, R.string.earn_message_TEMPLATE)
                .put("earn", earnMessagePointsOrPrice).format().toString()
    } else return ""
}