package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.Money

data class PointsAndCurrency(var points: Int, var pointsType: PointsType, var amount: Money)
