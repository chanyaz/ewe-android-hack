package com.expedia.vm.flights

import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.squareup.phrase.Phrase
import android.content.Context
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.utils.FlightV2Utils.getDeltaPricing
import rx.subjects.PublishSubject

class FareFamilyItemViewModel(val context: Context,
                              val fareFamilyDetail: FlightTripResponse.FareFamilyDetails,
                              val defaultChecked: Boolean, val roundTripObservable: PublishSubject<Boolean>) {

    val radioBtnClickObservable = PublishSubject.create<Unit>()
    val choosingFareFamilyObservable = radioBtnClickObservable.map { fareFamilyDetail }

    val fareFamilyName = fareFamilyDetail.fareFamilyName
    val fareFamilyCode = fareFamilyDetail.fareFamilyCode
    val fareDeltaAmount = getDeltaPricing(fareFamilyDetail.deltaTotalPrice, fareFamilyDetail.deltaPositive)
    val cabinClass = createCabinClass()

    private fun createCabinClass(): String {
        return Phrase.from(context, R.string.cabin_code_TEMPLATE).put("cabincode",
                context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(fareFamilyDetail.cabinClass.toLowerCase())))
                .format().toString()
    }
}
