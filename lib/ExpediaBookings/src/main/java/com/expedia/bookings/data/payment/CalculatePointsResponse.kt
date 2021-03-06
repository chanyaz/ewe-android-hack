package com.expedia.bookings.data.payment

import com.expedia.bookings.data.cars.BaseApiResponse

class CalculatePointsResponse : BaseApiResponse() {
    val programName: ProgramName? = null
    val conversion: PointsAndCurrency? = null
    val remainingPayableByCard: PointsAndCurrency? = null
}

