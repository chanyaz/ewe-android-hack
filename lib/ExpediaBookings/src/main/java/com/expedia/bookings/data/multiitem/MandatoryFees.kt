package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class MandatoryFees(
    val totalMandatoryFeesSupplyCurrency: Money,
    val dailyResortFeePOSCurrency: Money,
    val displayType: DisplayType
) {
    enum class DisplayType {
        TOTAL, DAILY, NONE
    }
}
