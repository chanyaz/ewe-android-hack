package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import rx.subjects.BehaviorSubject

public class PackageFlightViewModel(private val context: Context, private val flight: FlightLeg) {
    val resources = context.resources

    val flightTimeObserver = BehaviorSubject.create(PackageFlightUtils.getFlightDepartureArivalTimeAndDays(context, flight))
    val priceObserver = BehaviorSubject.create(if (flight.packageOfferModel.price.deltaPositive) ("+" + flight.packageOfferModel.price.differentialPriceFormatted) else flight.packageOfferModel.price.differentialPriceFormatted)
    val airlineObserver = BehaviorSubject.create(PackageFlightUtils.getDistinctiveAirline(flight.airlines))
    val durationObserver = BehaviorSubject.create(PackageFlightUtils.getFlightDurationStopString(context, flight))
    val layoverObserver = BehaviorSubject.create(flight)

}
