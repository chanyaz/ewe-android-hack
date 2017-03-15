package com.expedia.bookings.mia

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealRequest
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.services.sos.SmartOfferService
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject

class MemberDealResponseProvider(private val smartOfferService: SmartOfferService) {

    val dealsObserver = DealsObserver()
    val errorSubject = PublishSubject.create<Unit>()
    val memberDealResponseSubject = PublishSubject.create<MemberDealResponse>()
    private var searchSubscription: Subscription? = null
    private var returnedResponse: MemberDealResponse? = null

    init {
        memberDealResponseSubject.subscribe { response ->
            returnedResponse = response
        }
    }

    fun fetchDeals() {
        if (returnedResponse != null) {
            memberDealResponseSubject.onNext(returnedResponse)
        }
        else {
            val request = MemberDealRequest()
            request.siteId = PointOfSale.getPointOfSale().tpid.toString()
            searchSubscription = smartOfferService.fetchMemberDeals(request, dealsObserver)
        }
    }

    inner class DealsObserver : Observer<MemberDealResponse> {
        override fun onError(e: Throwable?) {
            errorSubject.onNext(Unit)
        }

        override fun onNext(response: MemberDealResponse?) {
            if (response == null || response.hasError()) {
                errorSubject.onNext(Unit)
            } else {
                memberDealResponseSubject.onNext(response)
            }
        }

        override fun onCompleted() {
            cleanup()
        }
    }

    private fun cleanup() {
        if (searchSubscription != null) {
            searchSubscription!!.unsubscribe()
            searchSubscription = null
        }
    }
}