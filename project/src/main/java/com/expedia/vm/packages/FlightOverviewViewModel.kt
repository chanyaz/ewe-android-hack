package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.vm.AbstractFlightOverviewViewModel
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.create(true)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.differentialPriceFormatted
    }
}