package com.expedia.bookings.itin.vm


class FlightItinLinkOffCardViewViewModel: ItinLinkOffCardViewViewModel() {
    override fun updateCardView(params: CardViewParams) = cardViewParamsSubject.onNext(params)

}