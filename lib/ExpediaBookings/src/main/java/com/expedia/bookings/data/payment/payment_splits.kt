package com.expedia.bookings.data.payment

class PaymentSplits(val payingWithPoints: PointsAndCurrency, val payingWithCards: PointsAndCurrency) {

    fun paymentSplitsType(): PaymentSplitsType {
        when {
            this.payingWithCards.amount.isZero -> return PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT
            this.payingWithPoints.amount.isZero -> return PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
            else -> return PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD
        }
    }
}
