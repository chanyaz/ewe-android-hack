package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilyDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import android.support.v4.content.ContextCompat
import rx.Observable
import java.util.Locale

class FareFamilyViewModel(private val context: Context) {
    // inputs
    val tripObservable = PublishSubject.create<FlightTripResponse>()
    val selectedFareFamilyObservable = PublishSubject.create<FlightTripResponse.FareFamilyDetails>()

    //outputs
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()
    val selectedClassObservable = PublishSubject.create<String>()
    val fareFamilyTitleObservable = PublishSubject.create<String>()
    val deltaPriceObservable = PublishSubject.create<String>()
    val selectedClassColorObservable = PublishSubject.create<Int>()
    val fareFamilyCardClickObserver = PublishSubject.create<Unit>()
    val contentDescriptionObservable = PublishSubject.create<String>()
    val fromLabelVisibility = PublishSubject.create<Boolean>()
    val travellerObservable = PublishSubject.create<String>()
    val updateTripObserver = PublishSubject.create<Pair<String, FlightTripResponse.FareFamilyDetails>>()
    val isUserBucketedForFareFamily = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)

    init {
        tripObservable.subscribe { trip ->
            val fareFamilyDetail = getDeltaPricingFareFamily(trip.fareFamilyList?.fareFamilyDetails)
            if (fareFamilyDetail != null) {
                widgetVisibilityObservable.onNext(isUserBucketedForFareFamily && !trip.getOffer().isSplitTicket)
                if (!trip.isFareFamilyUpgraded) {
                    selectedClassObservable.onNext(FlightV2Utils.getSelectedClassesString(context, trip.details))
                    deltaPriceObservable.onNext(FlightV2Utils.getDeltaPricing(fareFamilyDetail.deltaTotalPrice, fareFamilyDetail.deltaPositive))
                    fromLabelVisibility.onNext(true)
                    travellerObservable.onNext(StrUtils.formatMultipleTravelerString(context, Db.getFlightSearchParams().guests))
                    fareFamilyTitleObservable.onNext(
                            if (Db.getFlightSearchParams().isRoundTrip()) {
                                context.getString(R.string.flight_fare_family_upgrade_flight_roundtrip_label)
                            } else {
                                context.getString(R.string.flight_fare_family_upgrade_flight_oneway_label)
                            })
                    selectedClassColorObservable.onNext(ContextCompat.getColor(context, R.color.default_text_color))
                } else {
                    selectedClassObservable.onNext(context.getString(R.string.flight_change_fare_class))
                    deltaPriceObservable.onNext("")
                    travellerObservable.onNext("")
                    fromLabelVisibility.onNext(false)
                    selectedClassColorObservable.onNext(ContextCompat.getColor(context, R.color.app_primary))
                }
            } else {
                widgetVisibilityObservable.onNext(false)
            }
        }

        tripObservable.withLatestFrom(selectedFareFamilyObservable, { trip, fareDetails ->
            object {
                val isFareFamilyUpgraded = trip.isFareFamilyUpgraded
                val fareFamilyName = fareDetails.fareFamilyName
            }
        })
                .filter { it.isFareFamilyUpgraded }
                .subscribe {
                    fareFamilyTitleObservable.onNext(Phrase.from(context, R.string.flight_fare_family_fare_label_TEMPLATE).put("fare_family_name",
                            Strings.capitalize(it.fareFamilyName, Locale.US)).format().toString())
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

        Observable.combineLatest(selectedClassObservable, deltaPriceObservable, fareFamilyTitleObservable, tripObservable,
                {
                    selectedClass, deltaPrice, upgradeTitle, trip ->
                    object {
                        val selectedClass = selectedClass
                        val deltaPrice = deltaPrice
                        val upgradeTitle = upgradeTitle
                        val isFareFamilyUpgraded = trip.isFareFamilyUpgraded
                    }
                })
                .subscribe {
                    if (!it.isFareFamilyUpgraded) {
                        contentDescriptionObservable.onNext(
                                Phrase.from(context, R.string.flight_fare_family_upgrade_flight_cont_desc)
                                        .put("upgrade_title", it.upgradeTitle)
                                        .put("amount", it.deltaPrice)
                                        .put("guest", StrUtils.formatTravelerString(context, Db.getFlightSearchParams().guests))
                                        .put("class", it.selectedClass).format().toString())
                    } else {
                        contentDescriptionObservable.onNext(
                                Phrase.from(context, R.string.flight_fare_family_change_cont_desc)
                                        .put("current_selection", it.upgradeTitle)
                                        .format().toString())
                    }
                }
    }

    private fun getDeltaPricingFareFamily(fareFamilyDetails: Array<FareFamilyDetails>?): FareFamilyDetails? {
        if (fareFamilyDetails != null) {
            if (fareFamilyDetails.size > 1) {
                return fareFamilyDetails[1]
            } else if (fareFamilyDetails.size == 1) {
                return fareFamilyDetails[0]
            }
        }
        return null
    }
}