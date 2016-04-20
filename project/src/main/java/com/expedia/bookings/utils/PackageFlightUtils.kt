package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.squareup.phrase.Phrase

object PackageFlightUtils {

    @JvmStatic fun getFlightDurationStopString(context: Context, flight: FlightLeg): String {
        return context.resources.getString(R.string.flight_duration_description_template, getFlightDurationString(context, flight), getFlightStopString(context, flight))
    }

    @JvmStatic fun getFlightSegmentDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.durationHours, segment.durationMinutes)
    }

    @JvmStatic fun getFlightSegmentLayoverDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.layoverDurationHours, segment.layoverDurationMinutes)
    }

    @JvmStatic fun getFlightDurationString(context: Context, flight: FlightLeg): String {
        return getDurationString(context, flight.durationHour, flight.durationMinute)
    }

    private fun getDurationString(context: Context, durationHour: Int, durationMinute: Int): String {
        if (durationHour > 0) {
            return context.resources.getString(R.string.flight_hour_min_duration_template, durationHour, durationMinute)
        }
        return context.resources.getString(R.string.flight_min_duration_template, durationMinute)
    }

    @JvmStatic fun getFlightStopString(context: Context, flight: FlightLeg): String {
        val numOfStops = flight.stopCount
        if (numOfStops == 0) {
            return context.resources.getString(R.string.flight_nonstop_description)
        } else {
            return context.resources.getQuantityString(R.plurals.x_Stops_TEMPLATE, numOfStops, numOfStops)
        }
    }

    @JvmStatic fun getFlightDepartureArivalTimeAndDays(context: Context, flight: FlightLeg): String {
        return getFlightDepartureArivalTimeAndDays(context, flight.departureDateTimeISO, flight.arrivalDateTimeISO, flight.elapsedDays)
    }

    @JvmStatic fun getFlightDepartureArivalTimeAndDays(context: Context, departureTime: String, arrivalTime: String, elapsedDays: Int): String {
        if (elapsedDays > 0) {
            return context.resources.getString(R.string.flight_departure_arrival_time_multi_day_template,
                    DateUtils.formatTimeShort(departureTime), DateUtils.formatTimeShort(arrivalTime), elapsedDays)
        }
        return getFlightDepartureArivalTime(context, DateUtils.formatTimeShort(departureTime), DateUtils.formatTimeShort(arrivalTime))
    }

    @JvmStatic fun getFlightDepartureArivalTime(context: Context, departureTime: String, arrivalTime: String): String {
        return context.resources.getString(R.string.flight_departure_arrival_time_template, departureTime, arrivalTime)
    }

    @JvmStatic fun getFlightDepartureArivalCityAirport(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_departure_arrival_TEMPLATE))
                .put("departurecity", flightSegment.departureCity)
                .put("departureairportcode", flightSegment.departureAirportCode)
                .put("arrivalcity", flightSegment.arrivalCity)
                .put("arrivalairportcode", flightSegment.arrivalAirportCode)
                .format().toString()
    }

    @JvmStatic fun getFlightAirlineAndAirplaneType(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_airline_airplane_TEMPLATE))
                .put("carrier", flightSegment.carrier)
                .put("flightnumber", flightSegment.flightNumber)
                .put("airplanetype", Strings.capitalize(flightSegment.airplaneType))
                .format().toString()
    }

    @JvmStatic fun getDistinctiveAirline(airlines: List<Airline>): List<Airline> {
        if (airlines.all { it.airlineName == airlines[0].airlineName } ) {
            return airlines.subList(0, 1)
        }
        return airlines
    }

    @JvmStatic fun isFlightMerchant(flightLeg: FlightLeg): Boolean {
        // https://confluence/display/Omniture/Products+String+and+Events#ProductsStringandEvents-Flights
        when (flightLeg.flightFareTypeString.toUpperCase()) {
            "M", "SN", "N", "WP", "WPNS", "W", "SM" -> return true
            "C", "L", "CN", "PP", "P" -> return false
        }
        return false
    }
}
