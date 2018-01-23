package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.Money

data class PriceRange(val currencyCode: String, val minPrice: Int, val maxPrice: Int) {
    val notches = 30
    val defaultMinPriceText = formatValue(toValue(minPrice))
    val defaultMaxPriceText = formatValue(toValue(maxPrice))

    private fun toValue(price: Int): Int = (((price.toFloat() - minPrice) / maxPrice) * notches).toInt()
    private fun toPrice(value: Int): Int = ((value.toFloat() / notches) * (maxPrice - minPrice) + minPrice).toInt()
    fun formatValue(value: Int): String {
        val price = toPrice(value)
        val str = Money(toPrice(value), currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        if (price == maxPrice) {
            return str + "+"
        } else {
            return str
        }
    }

    fun getUpdatedPriceRange(minValue: Int, maxValue: Int): Pair<Int, Int> {
        val newMaxPrice = toPrice(maxValue)
        return Pair(toPrice(minValue), if (newMaxPrice == maxPrice) 0 else newMaxPrice)
    }
}
