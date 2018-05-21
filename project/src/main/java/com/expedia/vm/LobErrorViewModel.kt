package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.ApiError
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class LobErrorViewModel(context: Context) : BaseErrorViewModel(context) {

    // Inputs
    val searchApiErrorObserver = PublishSubject.create<ApiError>()
    val checkoutApiErrorObserver = PublishSubject.create<ApiError>()
    val createTripErrorObserverable = PublishSubject.create<ApiError>()

    // handle different errors
    val checkoutCardErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutTravelerErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutUnknownErrorObservable = BehaviorSubject.create<Unit>()
    val productKeyExpiryObservable = BehaviorSubject.create<Unit>()
    val checkoutAlreadyBookedObservable = BehaviorSubject.create<Unit>()
    val checkoutPaymentFailedObservable = BehaviorSubject.create<Unit>()
    val sessionTimeOutObservable = BehaviorSubject.create<Unit>()
    val soldOutObservable = BehaviorSubject.create<Unit>()
    val createTripUnknownErrorObservable = BehaviorSubject.create<Unit>()
    val filterNoResultsObservable = BehaviorSubject.create<Unit>()

    init {
        checkoutApiErrorObserver.subscribe(checkoutApiErrorHandler())
        createTripErrorObserverable.subscribe(createTripErrorHandler())
        searchApiErrorObserver.subscribe(searchErrorHandler())
    }

    protected abstract fun searchErrorHandler(): Observer<ApiError>
    protected abstract fun createTripErrorHandler(): Observer<ApiError>
    protected abstract fun checkoutApiErrorHandler(): Observer<ApiError>
}
