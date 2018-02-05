package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class MandatoryFees(
    val totalMandatoryFeesSupplyCurrency: Money,
    val totalMandatoryFeesPOSCurrency: Money,
    val dailyResortFeePOSCurrency: Money,
    val displayType: DisplayType,
    val displayCurrency: DisplayCurrency
) {
    enum class DisplayType {
        TOTAL, DAILY, NONE
    }

    enum class DisplayCurrency {
        POINT_OF_SUPPLY, POINT_OF_SALE
    }
}
