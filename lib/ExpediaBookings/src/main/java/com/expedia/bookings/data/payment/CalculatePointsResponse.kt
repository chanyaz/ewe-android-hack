package com.expedia.bookings.data.payment

import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money

class CalculatePointsResponse : BaseApiResponse() {
    val programName: ProgramName? = null
    val conversion: PointsAndCurrency? = null
    val remainingPayableByCard: PointsAndCurrency? = null
    val tripTotalPayable: Money? = null
}
