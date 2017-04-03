package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.FeatureToggleUtil
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel(context: Context) {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val posAirlineCouldChargeFees =
            if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_payment_legal_message))
                PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage() else
                PointOfSale.getPointOfSale().shouldShowAirlinePaymentMethodFeeMessage()

    init {
        isOutboundResults.subscribe { isOutbound ->
                airlineChargesFeesSubject.onNext(posAirlineCouldChargeFees)
        }
    }
}
