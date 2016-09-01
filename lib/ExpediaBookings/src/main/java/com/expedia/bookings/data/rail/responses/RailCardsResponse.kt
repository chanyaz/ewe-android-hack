package com.expedia.bookings.data.rail.responses

class RailCardsResponse {
    val railCards = emptyList<RailCard>()
}

data class RailCard(
        val category: String, val program: String, val name: String
)

data class RailCardSelected(
        val id: Int, val cardType: RailCard, val quantity: Int
)