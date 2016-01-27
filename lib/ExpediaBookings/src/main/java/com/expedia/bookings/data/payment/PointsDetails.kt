package com.expedia.bookings.data.payment

data class PointsDetails(
        val programName: PointsProgramType,
        val isAllowedToRedeem: Boolean,
        val minimumPointsRequiredToRedeem: Int,
        val totalAvailable: PointsAndCurrency,
        val maxPayableWithPoints: PointsAndCurrency?,
        val remainingPayableByCard: PointsAndCurrency?,
        val paymentsInstrumentsId: String?,
        val rateID: String
)