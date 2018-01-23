package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money

class UserPaymentPreferences(
        val amountOnPointsCard: List<UserPreferencePointsDetails>,
        val remainingPayableByCard: PointsAndCurrency,
        val tripTotalPayable: Money) {

    fun getUserPreference(programName: ProgramName): PointsAndCurrency? {
        amountOnPointsCard.forEach {
            if (it.programName == programName) {
                return it.payableByPoints
            }
        }
        return null
    }
}
