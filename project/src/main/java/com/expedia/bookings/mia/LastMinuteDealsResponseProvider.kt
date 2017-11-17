package com.expedia.bookings.mia

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.LastMinuteDealsRequest
import com.expedia.bookings.services.sos.SmartOfferService

class LastMinuteDealsResponseProvider(private val smartOfferService: SmartOfferService) : DealsResponseProvider() {
    override fun fetchDeals() {
        if (dealsResponseSubject != null) {
            dealsResponseSubject.onNext(dealsReturnedResponse)
        }
        else {
            val request = LastMinuteDealsRequest()
            val pos = PointOfSale.getPointOfSale()
            request.siteId = pos.tpid.toString()
            request.locale = pos.localeIdentifier.toString()
            searchSubscription = smartOfferService.fetchLastMinuteDeals(request, dealsObserver)
        }
    }
}