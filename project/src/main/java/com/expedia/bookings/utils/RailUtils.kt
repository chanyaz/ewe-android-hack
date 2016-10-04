package com.expedia.bookings.utils

import com.expedia.bookings.data.Money

object RailUtils {

    @JvmStatic
    fun addAndFormatMoney(firstPrice: Money, secondPrice: Money) : String {
        val price = Money(firstPrice)
        var formattedPrice = ""
        if (price.add(secondPrice.amount)) {
            formattedPrice = price.formattedMoney
        }
        return formattedPrice
    }
}