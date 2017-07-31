package com.expedia.vm.flights

import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.squareup.phrase.Phrase
import android.content.Context
import com.expedia.bookings.data.flights.FlightServiceClassType
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject


class FareFamilyItemViewModel(val context: Context, val fareFamilyDetail: FlightTripResponse.FareFamilyDetails,
                              var choosingFareFamilyObservable: BehaviorSubject<FlightTripResponse.FareFamilyDetails>, val markChecked: Boolean, val roundTripObservable: PublishSubject<Boolean>) {

    val fareFamilyName: String get() = fareFamilyDetail.fareFamilyName
    val fareFamilyCode: String get() = fareFamilyDetail.fareFamilyCode
    val fareDeltaAmount: String get() = createDeltaAmount()
    val cabinClass: String get() = createCabinClass()

    private fun createDeltaAmount(): String {
        return fareFamilyDetail.deltaTotalPrice.formattedWholePrice
    }

    private fun createCabinClass(): String {
        return Phrase.from(context, R.string.cabin_code).put("cabincode",
                context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(fareFamilyDetail.cabinClass.toLowerCase())))
                .format().toString()
    }

}
