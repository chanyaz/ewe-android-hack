package com.expedia.bookings.data.payment

class UserPaymentPreferences(
    val amountOnPointsCard : List<UserPreferencePointsDetails>,
    val remainingPayableByCard : PointsAndCurrency) {

    fun getUserPreference(programName: ProgramName): PointsAndCurrency? {
        amountOnPointsCard.forEach {
            if (it.programName == programName) {
                return it.payableByPoints
            }
        }
        return null
    }
}

data class UserPreferencePointsDetails (
    val programName: ProgramName,
    val payableByPoints : PointsAndCurrency
)