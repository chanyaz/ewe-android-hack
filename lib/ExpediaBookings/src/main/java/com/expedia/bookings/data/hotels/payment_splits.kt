package com.expedia.bookings.data.hotels

public class PaymentSplits(
        val payingWithPoints: PointsAndCurrency,
        val payingWithCards: PointsAndCurrency
) {
    fun paymentSplitsType(): com.expedia.bookings.data.hotels.PaymentSplitsType {
        when {
            this.payingWithCards.amount.isZero -> return com.expedia.bookings.data.hotels.PaymentSplitsType.IS_FULL_PAYABLE_WITH_EXPEDIA_POINT
            this.payingWithPoints.amount.isZero -> return com.expedia.bookings.data.hotels.PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
            else -> return com.expedia.bookings.data.hotels.PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD
        }
    }
}

public enum class PaymentSplitsType {
    IS_FULL_PAYABLE_WITH_EXPEDIA_POINT,
    IS_FULL_PAYABLE_WITH_CARD,
    IS_PARTIAL_PAYABLE_WITH_CARD
}