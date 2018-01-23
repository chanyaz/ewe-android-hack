package com.expedia.bookings.server

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.services.RailServices
import io.reactivex.Observer

class RailCardFeeServiceProvider {

    var lastFetchedTripId: String? = null
    var lastFetchedCardId: String? = null
    var lastFetchedTdoToken: String? = null

    fun fetchCardFees(railServices: RailServices, tripId: String, cardId: String, tdoToken: String, callback: Observer<CardFeeResponse>) {

        val fetchFreshCardFee = !(tripId == lastFetchedTripId && cardId == lastFetchedCardId && tdoToken == lastFetchedTdoToken)
        if (fetchFreshCardFee) {
            lastFetchedTripId = tripId
            lastFetchedCardId = cardId
            lastFetchedTdoToken = tdoToken
            railServices.railGetCardFees(tripId, cardId, tdoToken, callback)
        }
    }

    fun resetCardFees(railServices: RailServices) {
        lastFetchedTripId = null
        lastFetchedCardId = null
        lastFetchedTdoToken = null
        railServices.cancel()
    }
}
