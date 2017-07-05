package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class Promotion(
        val description: String,
        val amount: Money
)