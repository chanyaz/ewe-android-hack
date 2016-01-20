package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.Airline
import com.expedia.bookings.data.packages.FlightLeg

public object PackageFlightUtils {

    @JvmStatic public fun getAllAirports(flight: FlightLeg): String {
        var airports : String = ""
        for (segment in flight.flightSegments) {
            airports += segment.departureAirportCode + " "
        }
        airports += flight.flightSegments.last().arrivalAirportCode
        return airports
    }

    @JvmStatic public fun getFlightDurationStopString(context: Context, flight: FlightLeg): String {
        return context.resources.getString(R.string.flight_duration_description_template, getFlightDurationString(context, flight), getFlightStopString(context, flight))
    }

    @JvmStatic public fun getFlightDurationString(context: Context, flight: FlightLeg): String {
        if (flight.durationHour > 0) {
            return context.getResources().getString(R.string.flight_hour_min_duration_template, flight.durationHour, flight.durationMinute)
        }
        return context.getResources().getString(R.string.flight_min_duration_template, flight.durationMinute)
    }

    @JvmStatic public fun getFlightStopString(context: Context, flight: FlightLeg): String {
        val numOfStops = flight.stopCount
        if (numOfStops == 0) {
            return context.getResources().getString(R.string.flight_nonstop_description)
        } else {
            return context.getResources().getQuantityString(R.plurals.x_Stops_TEMPLATE, numOfStops, numOfStops)
        }
    }

    @JvmStatic public fun getFlightDepartureArivalTimeAndDays(context: Context, flight: FlightLeg): String {
        if (flight.elapsedDays > 0) {
            return context.getResources().getString(R.string.flight_departure_arrival_time_multi_day_template, flight.departureTimeShort, flight.arrivalTimeShort, flight.elapsedDays)
        }
        return context.getResources().getString(R.string.flight_departure_arrival_time_template, flight.departureTimeShort, flight.arrivalTimeShort)
    }

    @JvmStatic public fun getDistinctiveAirline(airlines: List<Airline>) : List<Airline> {
        if (airlines.all{ it.airlineName == airlines[0].airlineName } ) {
            return airlines.subList(0, 1)
        }
        return airlines
    }

}
