package com.expedia.vm

import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.flights.FlightTripDetails
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class BaseCreateTripViewModel() {
    val performCreateTrip = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<TripResponse>()
    val bundleDatesObservable = BehaviorSubject.create<String>()
    val showCreateTripDialogObservable = PublishSubject.create<Boolean>()
    val createTripErrorObservable = PublishSubject.create<ApiError>()
    val priceChangeObservable = PublishSubject.create<TripResponse>()
}
