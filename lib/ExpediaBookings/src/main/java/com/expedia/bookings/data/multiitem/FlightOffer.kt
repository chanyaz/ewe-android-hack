package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.Money

data class FlightOffer(
        val piid: String,
        val productTokens: List<String>,
        val legIds: List<String>,
        val referenceBasePrice: Price,
        val referenceTaxesAndFees: Price,
        val referenceTotalPrice: Price,
        val splitTicket: Boolean,
        val seatsLeft: Int,
        val bookingSeatCount: Int
) {
    fun flightOfferReferenceTotalPrice(): Money {
        return referenceTotalPrice.toMoney()
    }
}
