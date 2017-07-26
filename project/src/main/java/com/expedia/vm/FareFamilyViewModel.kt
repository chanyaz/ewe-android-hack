package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilies
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.FlightV2Utils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FareFamilyViewModel(private val context: Context, val flightServices: FlightServices) {
    // inputs
    val tripObservable = BehaviorSubject.create<FlightTripResponse>()
    val doneObservable = PublishSubject.create<Unit>()
    val updateFareFamilyUiObservable = PublishSubject.create<Boolean>()

    //outputs
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()
    val selectedClassObservable = PublishSubject.create<CharSequence>()
    val fareFamilyTitleObservable = PublishSubject.create<String>()
    val selectedUpgradedProduct = BehaviorSubject.create<FlightTripResponse.FareFamilyDetails>()
    val deltaPriceObservable = PublishSubject.create<String>()
    val fromLabelVisibility = PublishSubject.create<Boolean>()
    val updateTripObserver = PublishSubject.create<Pair<String, FlightTripResponse.FareFamilyDetails>>()

    val isUserBucketedForFareFamily = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFareFamilyFlightSummary,
            R.string.preference_fare_family_flight_summary)

    init {
        tripObservable.subscribe { trip ->
            widgetVisibilityObservable.onNext(isUserBucketedForFareFamily && trip.fareFamilies != null && !trip.getOffer().isSplitTicket)
            if (trip.fareFamilies != null) {
                selectedClassObservable.onNext(FlightV2Utils.getSelectedClassesString(context, trip.details))
                if (!trip.isFareFamilySelected) {
                    deltaPriceObservable.onNext((trip.fareFamilies as FareFamilies).fareFamilyDetails[0].deltaTotalPrice.formattedWholePrice)
                    fromLabelVisibility.onNext(true)
                    fareFamilyTitleObservable.onNext(context.getString(R.string.flight_fare_family_upgrade_flight_label))
                } else {
                    deltaPriceObservable.onNext("")
                    fromLabelVisibility.onNext(false)
                    fareFamilyTitleObservable.onNext(context.getString(R.string.flight_fare_family_change_class_label))
                }
            }
        }
        doneObservable.subscribe {
            updateTripObserver.onNext(Pair(tripObservable.value.fareFamilies!!.productKey, tripObservable.value.fareFamilies!!.fareFamilyDetails[1]))
        }
    }
}