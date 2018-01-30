package com.expedia.bookings.mia

import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.os.OfferService
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class LastMinuteDealsResponseProvider(private val offerService: OfferService, private val tuid: Long?) {

    protected val lastMinuteDealObserver = LastMinuteDealObserver()
    val errorSubject = PublishSubject.create<Unit>()
    val dealsResponseSubject = PublishSubject.create<LastMinuteDealsResponse>()
    var searchSubscription: Disposable? = null
    protected var dealsReturnedResponse: LastMinuteDealsResponse? = null

    init {
        dealsResponseSubject.subscribe { response ->
            dealsReturnedResponse = response
        }
    }

    inner class LastMinuteDealObserver : DisposableObserver<LastMinuteDealsResponse>() {
        override fun onError(e: Throwable) {
            errorSubject.onNext(Unit)
        }

        override fun onNext(response: LastMinuteDealsResponse) = if (response.hasError()) {
            errorSubject.onNext(Unit)
        } else {
            dealsResponseSubject.onNext(response)
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

    fun fetchDeals() {
        if (dealsReturnedResponse != null) {
            dealsResponseSubject.onNext(dealsReturnedResponse!!)
        } else {
            val request = LastMinuteDealsRequest(tuid.toString())
            val pos = PointOfSale.getPointOfSale()
            request.siteId = pos.tpid.toString()
            request.locale = pos.localeIdentifier.toString()
            searchSubscription = offerService.fetchLastMinuteDeals(request, lastMinuteDealObserver)
        }
    }
}
