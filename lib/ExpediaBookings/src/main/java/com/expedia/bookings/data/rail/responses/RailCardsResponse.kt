package com.expedia.bookings.data.rail.responses

import com.expedia.bookings.utils.Strings

class RailCardsResponse {
    val railCards = emptyList<RailCard>()
}

data class RailCard(
        val category: String, val program: String, val name: String
)

class RailCardSelected(val id: Int, val cardType: RailCard, val quantity: Int) {

    fun isSelectionEmpty(): Boolean {
        return quantity == 0 && Strings.isEmpty(cardType.name)
    }

    fun isSelectionPartial(): Boolean {
        return quantity == 0 || Strings.isEmpty(cardType.name)
    }

    fun isResetState(): Boolean {
        return quantity != 0 || Strings.isNotEmpty(cardType.name)
    }
}
