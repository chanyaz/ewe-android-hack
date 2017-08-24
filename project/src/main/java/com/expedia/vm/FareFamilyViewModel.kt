package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilies
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.FlightV2Utils
import rx.subjects.PublishSubject

class FareFamilyViewModel(private val context: Context) {
    // inputs
    val tripObservable = PublishSubject.create<FlightTripResponse>()

    //outputs
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()
    val selectedClassObservable = PublishSubject.create<CharSequence>()
    val deltaPriceObservable = PublishSubject.create<String>()

    val isUserBucketedForFareFamily = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFareFamilyFlightSummary,
            R.string.preference_fare_family_flight_summary)

    init {
        tripObservable.subscribe { trip ->
            widgetVisibilityObservable.onNext(isUserBucketedForFareFamily && trip.fareFamilies != null && !trip.getOffer().isSplitTicket)
            if (trip.fareFamilies != null) {
                val fareFamilyDetails = (trip.fareFamilies as FareFamilies).fareFamilyDetails
                if (fareFamilyDetails != null && fareFamilyDetails.isNotEmpty()) {
                    deltaPriceObservable.onNext(fareFamilyDetails[0].deltaTotalPrice.formattedPrice)
                    selectedClassObservable.onNext(FlightV2Utils.getSelectedClassesString(context, trip.details))
                }
            }
        }
    }
}
