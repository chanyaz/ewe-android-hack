package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.FeatureToggleUtil
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel(context: Context, lob: LineOfBusiness) {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val shouldShowDeltaPricing = lob == LineOfBusiness.FLIGHTS_V2 && FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppFlightsDeltaPricing, R.string.preference_flight_delta_pricing)

    init {
        isOutboundResults.subscribe { isOutbound ->
                airlineChargesFeesSubject.onNext(PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())
        }
    }
}
