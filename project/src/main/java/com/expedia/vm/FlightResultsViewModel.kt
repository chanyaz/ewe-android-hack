package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel() {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
}

