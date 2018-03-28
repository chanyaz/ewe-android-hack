package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.Money

data class PriceRange(val currencyCode: String, val minPrice: Int, val maxPrice: Int) {
    val notches = 30
    val defaultMinPriceText = formatPrice(minPrice)
    val defaultMaxPriceText = formatPrice(maxPrice)

    fun toValue(price: Int): Int = (((price.toFloat() - minPrice) / maxPrice) * notches).toInt()
    fun toPrice(value: Int): Int = ((value.toFloat() / notches) * (maxPrice - minPrice) + minPrice).toInt()

    fun formatValue(value: Int): String {
        val price = toPrice(value)
        return formatPrice(price)
    }

    fun formatPrice(price: Int): String {
        var str = Money(price, currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        if (price == maxPrice) {
            return str + "+"
        }
        return str
    }

    fun getUpdatedPriceRange(minPrice: Int, maxPrice: Int): Pair<Int, Int> {
        return Pair(minPrice, if (maxPrice == this.maxPrice) 0 else maxPrice)
    }
}
