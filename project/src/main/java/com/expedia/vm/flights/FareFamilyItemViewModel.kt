package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.FlightV2Utils.getDeltaPricing
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class FareFamilyItemViewModel(
    val context: Context,
    val fareFamilyDetail: FlightTripResponse.FareFamilyDetails,
    val defaultChecked: Boolean,
    val roundTripObservable: PublishSubject<Boolean>
) {

    val radioBtnClickObservable = PublishSubject.create<Unit>()
    val showMoreVisibilitySubject = PublishSubject.create<Boolean>()
    val choosingFareFamilyObservable = radioBtnClickObservable.map { fareFamilyDetail }

    val fareFamilyName = fareFamilyDetail.fareFamilyName
    val fareFamilyCode = fareFamilyDetail.fareFamilyCode
    val fareDeltaAmount = getDeltaPricing(fareFamilyDetail.deltaTotalPrice, fareFamilyDetail.deltaPositive)
    val cabinClass = createCabinClass()
    val travelerTextObservable = PublishSubject.create<String>()
    val dividerVisibilitySubject = PublishSubject.create<Boolean>()

    private fun createCabinClass(): String {
        return Phrase.from(context, R.string.cabin_code_TEMPLATE).put("cabincode",
                context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(fareFamilyDetail.cabinClass.toLowerCase())))
                .format().toString()
    }

    fun getContentDescription(): String {
        val result = StringBuilder()
        result.append(Phrase.from(context, R.string.fare_family_item_cont_desc_TEMPLATE)
                .put("fare_family_name", fareFamilyName)
                .put("cabin_code", context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(fareFamilyDetail.cabinClass)))
                .format().toString())

        result.append(FlightV2Utils.getPrimaryAmenitiesContainerContDesc(context, fareFamilyDetail.fareFamilyComponents))

        result.append(Phrase.from(context, R.string.fare_family_item_cont_desc_price_TEMPLATE)
                .put("delta", Money.getFormattedMoneyFromAmountAndCurrencyCode(fareFamilyDetail.deltaTotalPrice.amount, fareFamilyDetail.deltaTotalPrice.currencyCode))
                .put("trip_type", context.getString(getTripType()))
                .put("guests", StrUtils.formatTravelerString(context, Db.getFlightSearchParams().guests))
                .format().toString())

        return result.toString()
    }

    fun getTripType(): Int {
        return if (Db.getFlightSearchParams().isRoundTrip()) R.string.flights_round_trip_label else R.string.flights_one_way_label
    }

    fun setShowMoreVisibility() {
        showMoreVisibilitySubject.onNext(FlightV2Utils.hasMoreAmenities(fareFamilyDetail.fareFamilyComponents))
    }
}
