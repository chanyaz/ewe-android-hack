package com.expedia.vm

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseResultsViewModel {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val updateFlightsStream = PublishSubject.create<Unit>()
    open val doNotOverrideFilterButton = false
    open val showLoadingStateV1 = false
    open val showRichContent = false
    val richContentGuide = PublishSubject.create<Unit>()
    val abortRichContentCallObservable = PublishSubject.create<Unit>()
    abstract fun getLineOfBusiness(): LineOfBusiness
}
