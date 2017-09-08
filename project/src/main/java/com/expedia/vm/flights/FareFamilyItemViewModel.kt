package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.FlightV2Utils.getDeltaPricing
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class FareFamilyItemViewModel(val context: Context,
                              val fareFamilyDetail: FlightTripResponse.FareFamilyDetails,
                              val defaultChecked: Boolean, val roundTripObservable: PublishSubject<Boolean>) {

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

    fun setShowMoreVisibility() {
        showMoreVisibilitySubject.onNext(FlightV2Utils.hasMoreAmenities(fareFamilyDetail.fareFamilyComponents))
    }
}
