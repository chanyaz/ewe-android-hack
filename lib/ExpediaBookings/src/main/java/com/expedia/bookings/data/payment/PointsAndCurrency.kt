package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money

//amountToEarn will not be null in case of remainingPayableByCard otherwise it will be null
data class PointsAndCurrency(var points: Float, var pointsType: PointsType, var amount: Money, var amountToEarn: Money? = null)
