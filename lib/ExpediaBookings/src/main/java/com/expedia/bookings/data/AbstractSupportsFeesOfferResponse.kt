package com.expedia.bookings.data

abstract class AbstractSupportsFeesOfferResponse: TripResponse() {
    open var totalPriceIncludingFees: Money? = null
    open var selectedCardFees: Money? = null
}
