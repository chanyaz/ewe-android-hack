package com.expedia.bookings.itin.flight.details

import com.expedia.bookings.itin.common.ItinLinkOffCardViewViewModel

class FlightItinLinkOffCardViewViewModel : ItinLinkOffCardViewViewModel() {
    override fun updateCardView(params: CardViewParams) = cardViewParamsSubject.onNext(params)
}
