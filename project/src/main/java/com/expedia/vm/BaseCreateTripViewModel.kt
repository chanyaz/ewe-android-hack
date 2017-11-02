package com.expedia.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.TripResponse
import com.expedia.util.Optional
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

    fun isValidContext(context: Context) : Boolean {
        return if (context is Activity) {
            !context.isDestroyed && !context.isFinishing
        } else {
            true
        }
    }
}
