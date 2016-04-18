package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import rx.subjects.BehaviorSubject

class PackageFlightViewModel(private val context: Context, private val flight: FlightLeg) {
    val resources = context.resources

    val flightTime = PackageFlightUtils.getFlightDepartureArivalTimeAndDays(context, flight)
    val price = if (flight.packageOfferModel.price.deltaPositive) ("+" + flight.packageOfferModel.price.differentialPriceFormatted) else flight.packageOfferModel.price.differentialPriceFormatted
    val airline = PackageFlightUtils.getDistinctiveAirline(flight.airlines)
    val duration = PackageFlightUtils.getFlightDurationStopString(context, flight)
    val layover = flight

}
