package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class FlightOffer(
        val piid: String,
        val productTokens: List<String>,
        val legIds: List<String>,
        val referenceBasePrice: Money,
        val referenceTaxesAndFees: Money,
        val referenceTotalPrice: Money,
        val splitTicket: Boolean,
        val seatsLeft: Int,
        val bookingSeatCount: Int
)