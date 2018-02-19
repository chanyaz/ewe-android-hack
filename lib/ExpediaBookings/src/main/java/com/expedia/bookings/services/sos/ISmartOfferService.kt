package com.expedia.bookings.services.sos

import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.data.sos.MemberDealsResponse
import io.reactivex.Observer

interface ISmartOfferService {
    fun fetchDeals(request: MemberDealsRequest, dealsObserver: Observer<MemberDealsResponse>)
}
