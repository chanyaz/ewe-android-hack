package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightResultsViewModel(context: Context, lob: LineOfBusiness) {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val shouldShowDeltaPricing = lob == LineOfBusiness.FLIGHTS_V2 &&
            AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsDeltaPricing)
    val doNotOverrideFilterButton = lob == LineOfBusiness.PACKAGES &&
            isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)

    init {
        isOutboundResults.subscribe { isOutbound ->
            airlineChargesFeesSubject.onNext(PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())
        }
    }
}
