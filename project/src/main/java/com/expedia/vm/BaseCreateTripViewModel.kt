package com.expedia.vm

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.TripResponse
import com.expedia.util.Optional
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class BaseCreateTripViewModel {
    val performCreateTrip = PublishSubject.create<Unit>()
    val createTripResponseObservable = BehaviorSubject.create<Optional<TripResponse>>()
    val bundleDatesObservable = BehaviorSubject.create<String>()
    val showCreateTripDialogObservable = PublishSubject.create<Boolean>()
    val createTripErrorObservable = PublishSubject.create<ApiError>()
    val showPriceChangeAlertObservable = PublishSubject.create<Boolean>()
    val priceChangeAlertPriceObservable =  PublishSubject.create<Optional<TripResponse>>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val updateOverviewUiObservable = PublishSubject.create<TripResponse>()

    fun reset() {
        createTripResponseObservable.onNext(Optional(null))
    }

}
