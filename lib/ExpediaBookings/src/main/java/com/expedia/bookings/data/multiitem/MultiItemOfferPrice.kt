package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class MultiItemOfferPrice(
    val basePrice: Price,
    val taxesAndFees: Price,
    val totalPrice: Price,
    val referenceBasePrice: Price,
    val referenceTaxesAndFees: Price,
    val referenceTotalPrice: Price,
    val savings: Price,
    val avgPricePerPerson: Price,
    val avgReferencePricePerPerson: Price,
    val deltaAvgPricePerPerson: Price?,
    val showSavings: Boolean
) {
    fun packageTotalPrice(): Money {
        return totalPrice.toMoney()
    }

    fun pricePerPerson(): Money {
        return avgPricePerPerson.toMoney()
    }

    fun strikeThroughPricePerPerson(): Money {
        return avgReferencePricePerPerson.toMoney()
    }

    fun packageSavings(): Money {
        return savings.toMoney()
    }

    fun deltaPricePerPerson(): Money? {
        return deltaAvgPricePerPerson?.toMoney()
    }
}
