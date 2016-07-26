package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel() {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()

    init {
        Observable.combineLatest(flightResultsObservable, isOutboundResults, { flightResults, isOutbound ->
            if (isOutbound) {
                val anOutboundLegHasObFees = flightResults.firstOrNull { it.mayChargeObFees } != null
                airlineChargesFeesSubject.onNext(anOutboundLegHasObFees)
            }
            else {
                airlineChargesFeesSubject.onNext(false)
            }
        }).subscribe()
    }
}
