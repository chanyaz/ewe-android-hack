package com.expedia.bookings.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.widget.packages.FlightAirlineWidget
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


    @JvmStatic fun getStylizedFlightDurationString(context: Context, flight: FlightLeg, colorId: Int): CharSequence {
        val flightDuration = PackageFlightUtils.getFlightDurationString(context, flight)
        var totalDuration = Phrase.from(context.resources.getString(R.string.package_flight_overview_total_duration_TEMPLATE))
                .put("duration", flightDuration)
                .format().toString()

        val start = totalDuration.indexOf(flightDuration)
        val end = start + flightDuration.length
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, colorId))
        val totalDurationStyledString = SpannableStringBuilder(totalDuration)
        totalDurationStyledString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        return totalDurationStyledString
    }

    @JvmStatic fun getOperatingAirlineNameString(context: Context, segment: FlightLeg.FlightSegment): String? {
        if (segment.operatingAirlineName != null) {
            if (segment.operatingAirlineCode != null) {
                return Phrase.from(context, R.string.flight_operated_by_carrier_name_and_code_TEMPLATE)
                        .put("operatingairlinename", segment.operatingAirlineName)
                        .put("operatingairlinecode", segment.operatingAirlineCode)
                        .format().toString()
            }
            return Phrase.from(context, R.string.flight_operated_by_carrier_name_only_TEMPLATE)
                    .put("operatingairlinename", segment.operatingAirlineName)
                    .format().toString()
        }
        return null
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

    @JvmStatic fun getFlightDepartureArrivalTimeAndDays(context: Context, flight: FlightLeg): String {
        return getFlightDepartureArrivalTimeAndDays(context, flight.departureDateTimeISO, flight.arrivalDateTimeISO, flight.elapsedDays)
    }

    @JvmStatic fun getFlightDepartureArrivalTimeAndDays(context: Context, departureTime: String, arrivalTime: String, elapsedDays: Int): String {
        if (elapsedDays > 0) {
            return context.resources.getString(R.string.flight_departure_arrival_time_multi_day_template,
                    DateUtils.formatTimeShort(departureTime), DateUtils.formatTimeShort(arrivalTime), elapsedDays)
        }
        return getFlightDepartureArrivalTime(context, DateUtils.formatTimeShort(departureTime), DateUtils.formatTimeShort(arrivalTime))
    }

    @JvmStatic fun getAccessibleDepartArrivalTime(context: Context, flight: FlightLeg): String {
        val departureTime = DateUtils.formatTimeShort(flight.departureDateTimeISO)
        val arrivalTime = DateUtils.formatTimeShort(flight.arrivalDateTimeISO)
        val elapsedDays = flight.elapsedDays
        if (elapsedDays > 0) {
            return Phrase.from(context, R.string.flight_departure_arrival_time_multi_day_cont_desc_TEMPLATE)
                    .put("departuretime", departureTime)
                    .put("arrivaltime", arrivalTime)
                    .put("elapseddays", elapsedDays).format().toString()
        }
        return Phrase.from(context, R.string.flight_departure_arrival_time_cont_desc_TEMPLATE)
                .put("departuretime", departureTime)
                .put("arrivaltime", arrivalTime).format().toString()
    }

    @JvmStatic fun getFlightDepartureArrivalTime(context: Context, departureTime: String, arrivalTime: String): String {
        return context.resources.getString(R.string.flight_departure_arrival_time_template, departureTime, arrivalTime)
    }

    @JvmStatic fun getFlightDepartureArrivalCityAirport(context: Context, flightSegment: FlightLeg.FlightSegment): String {
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

    @JvmStatic fun getAirlinesList(airlines: List<Airline>): String {
        var airlineList = StringBuilder()
        for (i in 0..airlines.size - 1) {
            airlineList.append(airlines[i].airlineName)
        }
        return airlineList.toString()
    }
}
