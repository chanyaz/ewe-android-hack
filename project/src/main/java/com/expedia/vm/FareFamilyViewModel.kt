package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.FlightV2Utils
import rx.subjects.PublishSubject

class FareFamilyViewModel(private val context: Context) {
    // inputs
    val tripObservable = PublishSubject.create<FlightTripResponse>()
    val selectedFareFamilyObservable = PublishSubject.create<FlightTripResponse.FareFamilyDetails>()

    //outputs
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()
    val selectedClassObservable = PublishSubject.create<String>()
    val fareFamilyTitleObservable = PublishSubject.create<String>()
    val deltaPriceObservable = PublishSubject.create<String>()
    val fareFamilyCardClickObserver = PublishSubject.create<Unit>()
    val fromLabelVisibility = PublishSubject.create<Boolean>()
    val updateTripObserver = PublishSubject.create<Pair<String, FlightTripResponse.FareFamilyDetails>>()
    val isUserBucketedForFareFamily = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFareFamilyFlightSummary,
            R.string.preference_fare_family_flight_summary)

    init {
        tripObservable.subscribe { trip ->
            val fareFamilyDetail = trip.fareFamilyList?.fareFamilyDetails?.firstOrNull()
            if (fareFamilyDetail != null) {
                widgetVisibilityObservable.onNext(isUserBucketedForFareFamily && !trip.getOffer().isSplitTicket)
                selectedClassObservable.onNext(FlightV2Utils.getSelectedClassesString(context, trip.details))
                if (!trip.isFareFamilyUpgraded) {
                    deltaPriceObservable.onNext(FlightV2Utils.getDeltaPricing(fareFamilyDetail.deltaTotalPrice, fareFamilyDetail.deltaPositive))
                    fromLabelVisibility.onNext(true)
                    fareFamilyTitleObservable.onNext(context.getString(R.string.flight_fare_family_upgrade_flight_label))
                } else {
                    deltaPriceObservable.onNext("")
                    fromLabelVisibility.onNext(false)
                    fareFamilyTitleObservable.onNext(context.getString(R.string.flight_fare_family_change_class_label))
                }
            } else {
                widgetVisibilityObservable.onNext(false)
            }
        }
        selectedFareFamilyObservable.withLatestFrom(tripObservable, { fareDetails, tripResponse ->
            object {
                val fareDetails = fareDetails
                val productKey = tripResponse.fareFamilyList?.productKey
            }
        })
                .filter { it.productKey != null }
                .map { Pair(it.productKey!!, it.fareDetails) }
                .subscribe(updateTripObserver)
    }
}