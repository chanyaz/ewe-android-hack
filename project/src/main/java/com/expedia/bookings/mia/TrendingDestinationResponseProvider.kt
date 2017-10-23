package com.expedia.bookings.mia

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealRequest
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.data.sos.TrendingDestinationRequest
import com.expedia.bookings.data.sos.TrendingDestinationResponse
import com.expedia.bookings.services.sos.SmartOfferService
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject

class TrendingDestinationResponseProvider(private val smartOfferService: SmartOfferService) {

    val dealsObserver = DealsObserver()
    val errorSubject = PublishSubject.create<Unit>()
    val trendingDestinationResponseSubject = PublishSubject.create<TrendingDestinationResponse>()
    private var searchSubscription: Subscription? = null
    private var returnedResponse: TrendingDestinationResponse? = null

    init {
        trendingDestinationResponseSubject.subscribe { response ->
            returnedResponse = response
        }
    }

    fun fetchDeals() {
        if (returnedResponse != null) {
            trendingDestinationResponseSubject.onNext(returnedResponse)
        }
        else {
            val request = TrendingDestinationRequest()
            val pos = PointOfSale.getPointOfSale()
            request.siteId = pos.tpid.toString()
            request.locale = pos.localeIdentifier.toString()
            searchSubscription = smartOfferService.fetchTrendingDestination(request, dealsObserver)
        }
    }

    inner class DealsObserver : Observer<TrendingDestinationResponse> {
        override fun onError(e: Throwable?) {
            errorSubject.onNext(Unit)
        }

        override fun onNext(response: TrendingDestinationResponse?) {
            if (response == null || response.hasError()) {
                errorSubject.onNext(Unit)
            } else {
                trendingDestinationResponseSubject.onNext(response)
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
