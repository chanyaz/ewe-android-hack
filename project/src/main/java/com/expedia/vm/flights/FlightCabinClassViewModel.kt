package com.expedia.vm.flights

import com.expedia.bookings.data.flights.FlightServiceClassType
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCabinClassViewModel {
    val flightCabinClassObservable = BehaviorSubject.create<FlightServiceClassType.CabinCode>()
    val flightSelectedCabinClassIdObservable = PublishSubject.create<Int>()

    init {
        flightCabinClassObservable.onNext(FlightServiceClassType.CabinCode.COACH)
    }
}