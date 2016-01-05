package com.expedia.bookings.data.hotels

public data class PaymentSplits(
    val payingWithPoints: PointsAndCurrency,
    val payingWithCards: PointsAndCurrency
)

public enum class PaymentSplitsType {
    IS_FULL_PAYABLE_WITH_EXPEDIA_POINT,
    IS_FULL_PAYABLE_WITH_CARD,
    IS_PARTIAL_PAYABLE_WITH_CARD
}