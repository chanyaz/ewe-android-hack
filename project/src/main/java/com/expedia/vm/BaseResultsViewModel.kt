package com.expedia.vm

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseResultsViewModel {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    open val doNotOverrideFilterButton = false
    open val showLoadingStateV1 = false

    init {
        isOutboundResults.subscribe {
            airlineChargesFeesSubject.onNext(PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())
        }
    }

    abstract fun getLineOfBusiness(): LineOfBusiness
}
