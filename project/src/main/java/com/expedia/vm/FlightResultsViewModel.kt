package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel() {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()

    init {
        isOutboundResults.subscribe { isOutbound ->
            if (isOutbound) {
                val posAirlineCouldChargeFees = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()
                airlineChargesFeesSubject.onNext(posAirlineCouldChargeFees)
            }
            else {
                airlineChargesFeesSubject.onNext(false)
            }
        }
    }
}
