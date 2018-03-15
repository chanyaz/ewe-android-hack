package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money

data class PointsDetails(
    val programName: ProgramName?,
    val isAllowedToRedeem: Boolean,
    val minimumPointsRequiredToRedeem: Int,
    val totalAvailable: PointsAndCurrency,
    val maxPayableWithPoints: PointsAndCurrency?,
    val remainingPayableByCard: PointsAndCurrency?,
    val tripTotalPayable: Money?,
    val paymentsInstrumentsId: String?,
    val rateID: String
)
