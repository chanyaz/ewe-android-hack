package com.expedia.bookings.data.payment

data class UserPreferencePointsDetails (
    val programName: ProgramName,
    val payableByPoints: PointsAndCurrency
)
