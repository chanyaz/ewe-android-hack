package com.expedia.bookings.mia

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.services.sos.SmartOfferService

class MemberDealsResponseProvider(private val smartOfferService: SmartOfferService) : DealsResponseProvider() {
    override fun fetchDeals() {
        val dealsReturnedResponse = dealsReturnedResponse
        if (dealsReturnedResponse != null) {
            dealsResponseSubject.onNext(dealsReturnedResponse)
        } else {
            val request = MemberDealsRequest()
            val pos = PointOfSale.getPointOfSale()
            request.siteId = pos.tpid.toString()
            request.locale = pos.localeIdentifier.toString()
            searchSubscription = smartOfferService.fetchMemberDeals(request, dealsObserver)
        }
    }
}
