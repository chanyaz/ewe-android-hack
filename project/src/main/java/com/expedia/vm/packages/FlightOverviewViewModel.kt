package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.vm.AbstractFlightOverviewViewModel
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.create(true)
    override val showEarnMessage = BehaviorSubject.create(false)
    override val showSeatClassAndBookingCode = BehaviorSubject.create(false)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.differentialPriceFormatted
    }

    override fun showFlightDistance(selectedFlight: FlightLeg): Boolean {
        return false
    }
}