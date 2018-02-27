package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightResultsViewModel(context: Context, lob: LineOfBusiness) {

    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val isOutboundResults = BehaviorSubject.create<Boolean>()
    val airlineChargesFeesSubject = PublishSubject.create<Boolean>()
    val shouldShowDeltaPricing = lob == LineOfBusiness.FLIGHTS_V2 &&
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsDeltaPricing)
    val doNotOverrideFilterButton = lob == LineOfBusiness.PACKAGES &&
            isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)
    val showLoadingStateV1 = lob == LineOfBusiness.FLIGHTS_V2 &&
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)
    init {
        isOutboundResults.subscribe {
            airlineChargesFeesSubject.onNext(PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())
        }
    }
}
