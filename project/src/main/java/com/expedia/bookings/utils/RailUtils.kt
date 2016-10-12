package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money

object RailUtils {
    @JvmStatic
    fun formatRailChangesText(context: Context, changesCount: Int): String {
        if (changesCount == 0) {
            return context.getString(R.string.rail_direct)
        }
        return context.resources.getQuantityString(R.plurals.rail_changes_TEMPLATE, changesCount, changesCount)
    }

    @JvmStatic
    fun addAndFormatMoney(firstPrice: Money, secondPrice: Money): String {
        val price = Money(firstPrice)
        var formattedPrice = ""
        if (price.add(secondPrice.amount)) {
            formattedPrice = price.formattedMoney
        }
        return formattedPrice
    }

    @JvmStatic
    fun subtractAndFormatMoney(firstPrice: Money, secondPrice: Money): String {
        val price = Money(firstPrice)
        var formattedPrice = ""
        if (price.subtract(secondPrice.amount)) {
            formattedPrice = price.formattedMoney
        }
        return formattedPrice
    }
}