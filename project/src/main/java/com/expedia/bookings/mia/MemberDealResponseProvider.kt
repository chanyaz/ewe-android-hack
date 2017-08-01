package com.expedia.bookings.mia

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealRequest
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.services.sos.SmartOfferService
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class MemberDealResponseProvider(private val smartOfferService: SmartOfferService) {

    val dealsObserver = DealsObserver()
    val errorSubject = PublishSubject.create<Unit>()
    val memberDealResponseSubject = PublishSubject.create<MemberDealResponse>()
    private var searchSubscription: Disposable? = null
    private var returnedResponse: MemberDealResponse? = null

    init {
        memberDealResponseSubject.subscribe { response ->
            returnedResponse = response
        }
    }

    fun fetchDeals() {
        if (returnedResponse != null) {
            memberDealResponseSubject.onNext(returnedResponse!!)
        }
        else {
            val request = MemberDealRequest()
            val pos = PointOfSale.getPointOfSale()
            request.siteId = pos.tpid.toString()
            request.locale = pos.localeIdentifier.toString()
            searchSubscription = smartOfferService.fetchMemberDeals(request, dealsObserver)
        }
    }

    inner class DealsObserver : DisposableObserver<MemberDealResponse>() {
        override fun onError(e: Throwable) {
            errorSubject.onNext(Unit)
        }

        override fun onNext(response: MemberDealResponse) {
            if (response == null || response.hasError()) {
                errorSubject.onNext(Unit)
            } else {
                memberDealResponseSubject.onNext(response)
            }
        }

        override fun onComplete() {
            cleanup()
        }
    }

    private fun cleanup() {
        if (searchSubscription != null) {
            searchSubscription!!.dispose()
            searchSubscription = null
        }
    }
}