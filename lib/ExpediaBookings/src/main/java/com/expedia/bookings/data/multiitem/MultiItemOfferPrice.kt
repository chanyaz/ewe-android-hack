package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class MultiItemOfferPrice(
        val baseMoney: Money,
        val taxesAndFees: Money,
        val totalMoney: Money,
        val referenceBaseMoney: Money,
        val referenceTaxesAndFees: Money,
        val referenceTotalMoney: Money,
        val savings: Money,
        val MoneyAdjustmentAmount: Double,
        val MoneyAdjustmentTypes: List<String>
)