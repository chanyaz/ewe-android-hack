package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.vm.AbstractFlightViewModel

class PackageFlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        if (flightLeg.packageOfferModel.price.deltaPositive) {
            return ("+" + flightLeg.packageOfferModel.price.differentialPriceFormatted)
        } else {
            return flightLeg.packageOfferModel.price.differentialPriceFormatted
        }
    }

    override fun getUrgencyMessageVisibilty(): Boolean {
        return false
    }
}
