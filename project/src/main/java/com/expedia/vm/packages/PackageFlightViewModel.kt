package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import rx.subjects.BehaviorSubject

class PackageFlightViewModel(private val context: Context, private val flightLeg: FlightLeg) {
    val resources = context.resources

    val flightTime = PackageFlightUtils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
    val price = if (flightLeg.packageOfferModel.price.deltaPositive) ("+" + flightLeg.packageOfferModel.price.differentialPriceFormatted) else flightLeg.packageOfferModel.price.differentialPriceFormatted
    val airline = PackageFlightUtils.getDistinctiveAirline(flightLeg.airlines)
    val duration = PackageFlightUtils.getFlightDurationStopString(context, flightLeg)
    val layover = flightLeg
}
