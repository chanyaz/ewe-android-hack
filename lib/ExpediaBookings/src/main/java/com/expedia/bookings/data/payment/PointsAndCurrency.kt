package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money

data class PointsAndCurrency(var points: Float, var pointsType: PointsType, var amount: Money)
