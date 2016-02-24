package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.Airline
import com.expedia.bookings.data.packages.FlightLeg
import com.squareup.phrase.Phrase

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

    @JvmStatic public fun getFlightSegmentDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.durationHours, segment.durationMinutes)
    }

    @JvmStatic public fun getFlightSegmentLayoverDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.layoverDurationHours, segment.layoverDurationMinutes)
    }

    @JvmStatic public fun getFlightDurationString(context: Context, flight: FlightLeg): String {
        return getDurationString(context, flight.durationHour, flight.durationMinute)
    }

    private fun getDurationString(context: Context, durationHour: Int, durationMinute: Int): String {
        if (durationHour > 0) {
            return context.getResources().getString(R.string.flight_hour_min_duration_template, durationHour, durationMinute)
        }
        return context.getResources().getString(R.string.flight_min_duration_template, durationMinute)
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
            return context.getResources().getString(R.string.flight_departure_arrival_time_multi_day_template, DateUtils.formatTimeShort(flight.departureDateTimeISO), DateUtils.formatTimeShort(flight.arrivalDateTimeISO), flight.elapsedDays)
        }
        return getFlightDepartureArivalTime(context, DateUtils.formatTimeShort(flight.departureDateTimeISO), DateUtils.formatTimeShort(flight.arrivalDateTimeISO))
    }

    @JvmStatic public fun getFlightDepartureArivalTime(context: Context, departureTime: String, arrivalTime: String): String {
        return context.getResources().getString(R.string.flight_departure_arrival_time_template, departureTime, arrivalTime)
    }

    @JvmStatic public fun getFlightDepartureArivalCityAirport(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_departure_arrival_TEMPLATE))
                .put("departurecity", flightSegment.departureCity)
                .put("departureairportcode", flightSegment.departureAirportCode)
                .put("arrivalcity", flightSegment.arrivalCity)
                .put("arrivalairportcode", flightSegment.arrivalAirportCode)
                .format().toString()
    }

    @JvmStatic public fun getFlightAirlineAirplaneType(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_airline_airplane_TEMPLATE))
                .put("carrier", flightSegment.carrier)
                .put("flightnumber", flightSegment.flightNumber)
                .put("airplanetype", flightSegment.airplaneType)
                .format().toString()
    }

    @JvmStatic public fun getDistinctiveAirline(airlines: List<Airline>) : List<Airline> {
        if (airlines.all{ it.airlineName == airlines[0].airlineName } ) {
            return airlines.subList(0, 1)
        }
        return airlines
    }
}
