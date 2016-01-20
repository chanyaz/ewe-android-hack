package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.Airline
import com.expedia.bookings.data.packages.FlightLeg
import rx.subjects.BehaviorSubject
import kotlin.collections.all

public class PackageFlightViewModel(private val context: Context, private val flight: FlightLeg) {
    val resources = context.resources

    val flightTimeObserver = BehaviorSubject.create(getFlightDepartureArivalTimeAndDays(flight))
    val priceObserver = BehaviorSubject.create(if (flight.packageOfferModel.price.deltaPositive) ("+" + flight.packageOfferModel.price.differentialPriceFormatted) else flight.packageOfferModel.price.differentialPriceFormatted)
    val airlineObserver = BehaviorSubject.create(getDistinctiveAirline(flight.airlines))
    val durationObserver = BehaviorSubject.create(resources.getString(R.string.flight_duration_description_template, getFlightDurationString(flight), getFlightStopString(flight)))
    val layoverObserver = BehaviorSubject.create(flight)

    private fun getFlightDurationString(flight: FlightLeg): String {
        if (flight.durationHour > 0) {
            return resources.getString(R.string.flight_hour_min_duration_template, flight.durationHour, flight.durationMinute)
        }
        return resources.getString(R.string.flight_min_duration_template, flight.durationMinute)
    }

    private fun getFlightStopString(flight: FlightLeg): String {
        val numOfStops = flight.stopCount
        if (numOfStops == 0) {
            return resources.getString(R.string.flight_nonstop_description)
        } else {
            return resources.getQuantityString(R.plurals.x_Stops_TEMPLATE, numOfStops, numOfStops)
        }
    }

    private fun getFlightDepartureArivalTimeAndDays(flight: FlightLeg): String {
        if (flight.elapsedDays > 0) {
            return resources.getString(R.string.flight_departure_arrival_time_multi_day_template, flight.departureTimeShort, flight.arrivalTimeShort, flight.elapsedDays)
        }
        return resources.getString(R.string.flight_departure_arrival_time_template, flight.departureTimeShort, flight.arrivalTimeShort)
    }

    private fun getDistinctiveAirline(airlines: List<Airline>) : List<Airline> {
        if (airlines.all{ it.airlineName == airlines[0].airlineName } ) {
            return airlines.subList(0, 1)
        }
        return airlines
    }
}
