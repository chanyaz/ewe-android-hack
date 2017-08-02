package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class MultiItemOfferPrice(
        val basePrice: Price,
        val taxesAndFees: Price,
        val totalPrice: Price,
        val referenceBasePrice: Price,
        val referenceTaxesAndFees: Price,
        val referenceTotalPrice: Price,
        val savings: Price
) {
    fun priceToShowUsers(): Money {
        return totalPrice.toMoney()
    }

    fun strikeThroughPrice(): Money {
        return referenceTotalPrice.toMoney()
    }

    fun packageSavings(): Money {
        return savings.toMoney()
    }
}