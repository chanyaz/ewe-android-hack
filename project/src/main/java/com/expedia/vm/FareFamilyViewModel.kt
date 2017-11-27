package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilyDetails
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import android.support.v4.content.ContextCompat
import com.expedia.bookings.data.Money
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

    init {
        tripObservable.subscribe { trip ->
            val fareFamilyDetail = getDeltaPricingFareFamily(trip.fareFamilyList?.fareFamilyDetails)
            val isRoundTrip = Db.getFlightSearchParams().isRoundTrip()

            if (fareFamilyDetail != null) {
                widgetVisibilityObservable.onNext(!trip.getOffer().isSplitTicket)
                if (!trip.isFareFamilyUpgraded) {
                    selectedClassObservable.onNext(FlightV2Utils.getSelectedClassesString(context, trip.details, false))
                    deltaPriceObservable.onNext(FlightV2Utils.getDeltaPricing(fareFamilyDetail.deltaTotalPrice, fareFamilyDetail.deltaPositive))
                    fromLabelVisibility.onNext(true)
                    travellerObservable.onNext(StrUtils.formatMultipleTravelerString(context, Db.getFlightSearchParams().guests))
                    fareFamilyTitleObservable.onNext(
                            if (isRoundTrip) {
                                context.getString(R.string.flight_fare_family_upgrade_flight_roundtrip_label)
                            } else {
                                context.getString(R.string.flight_fare_family_upgrade_flight_oneway_label)
                            })
                    selectedClassColorObservable.onNext(ContextCompat.getColor(context, R.color.default_text_color))
                    contentDescriptionObservable.onNext(
                            Phrase.from(context,
                                    if (isRoundTrip) {
                                        R.string.flight_fare_family_upgrade_round_trip_flight_cont_desc_TEMPLATE
                                    } else {
                                        R.string.flight_fare_family_upgrade_one_way_flight_cont_desc_TEMPLATE
                                    })
                                    .put("amount", Money.getFormattedMoneyFromAmountAndCurrencyCode(fareFamilyDetail.deltaTotalPrice.amount, fareFamilyDetail.deltaTotalPrice.currencyCode))
                                    .put("guest", StrUtils.formatTravelerString(context, Db.getFlightSearchParams().guests))
                                    .put("class", FlightV2Utils.getSelectedClassesString(context, trip.details, true)).format().toString())

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
                    val fareFamilyName = Strings.capitalize(it.fareFamilyName, Locale.US)
                    fareFamilyTitleObservable.onNext(Phrase.from(context, R.string.flight_fare_family_fare_label_TEMPLATE).put("fare_family_name",
                            fareFamilyName).format().toString())
                    contentDescriptionObservable.onNext(Phrase.from(context, R.string.flight_fare_family_change_cont_desc_TEMPLATE)
                            .put("fare_family_name", fareFamilyName)
                            .format().toString())
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