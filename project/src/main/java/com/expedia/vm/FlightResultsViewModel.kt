package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg
import rx.subjects.BehaviorSubject

class FlightResultsViewModel() {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
}

