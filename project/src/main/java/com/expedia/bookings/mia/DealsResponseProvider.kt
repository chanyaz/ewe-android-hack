package com.expedia.bookings.mia

import com.expedia.bookings.data.sos.DealsResponse
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

abstract class DealsResponseProvider {

    protected val dealsObserver = DealsObserver()
    val errorSubject = PublishSubject.create<Unit>()
    val dealsResponseSubject = PublishSubject.create<DealsResponse>()
    var searchSubscription: Disposable? = null
    protected var dealsReturnedResponse: DealsResponse? = null

    init {
        dealsResponseSubject.subscribe { response ->
            dealsReturnedResponse = response
        }
    }

    open fun fetchDeals() {}

    inner class DealsObserver : DisposableObserver<DealsResponse>() {
        override fun onError(e: Throwable) {
            errorSubject.onNext(Unit)
        }

        override fun onNext(response: DealsResponse) {
            if (response == null || response.hasError()) {
                errorSubject.onNext(Unit)
            } else {
                dealsResponseSubject.onNext(response)
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