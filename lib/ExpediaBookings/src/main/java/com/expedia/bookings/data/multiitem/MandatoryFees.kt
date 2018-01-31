package com.expedia.bookings.data.multiitem

import java.math.BigDecimal

data class MandatoryFees(val totalMandatoryFeesSupplyCurrency: FeeAndCurrency,
                         val dailyResortFeePOSCurrency: FeeAndCurrency, val displayType: DisplayType) {
    enum class DisplayType {
        TOTAL, DAILY, NONE
    }
}

data class FeeAndCurrency(val amount: BigDecimal, val currency: String)
