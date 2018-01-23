package com.expedia.vm.flights

import com.expedia.bookings.data.flights.FlightServiceClassType
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightCabinClassViewModel {
    val flightCabinClassObservable = BehaviorSubject.create<FlightServiceClassType.CabinCode>()
    val flightSelectedCabinClassIdObservable = PublishSubject.create<Int>()

    init {
        flightCabinClassObservable.onNext(FlightServiceClassType.CabinCode.COACH)
    }
}
