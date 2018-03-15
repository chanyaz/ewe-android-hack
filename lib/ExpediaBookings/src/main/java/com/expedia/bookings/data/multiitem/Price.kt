package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money
import java.math.BigDecimal

data class Price(
    val amount: BigDecimal,
    val currency: String
) {
    fun toMoney(): Money {
        return Money(amount, currency)
    }
}
