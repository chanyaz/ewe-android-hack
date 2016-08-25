package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.SpannableBuilder
import com.squareup.phrase.Phrase

abstract class AbstractFlightViewModel(protected val context: Context, protected val flightLeg: FlightLeg) {
    val resources = context.resources
    val flightTime = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
    val asscesibleFlightTime = FlightV2Utils.getAccessibleDepartArrivalTime(context, flightLeg)
    val airline = FlightV2Utils.getDistinctiveAirline(flightLeg.airlines)
    val duration = FlightV2Utils.getFlightDurationStopString(context, flightLeg)
    val layover = flightLeg
    var flightSegments = flightLeg.flightSegments

    abstract fun price(): String

    var contentDescription = getFlightContentDesc()

    fun getFlightContentDesc(): CharSequence {
        var result = SpannableBuilder()

        result.append(Phrase.from(context, R.string.flight_detail_card_cont_desc_TEMPLATE)
                .put("time", asscesibleFlightTime)
                .put("pricedifference", price())
                .put("airline", FlightV2Utils.getAirlinesList(airline))
                .put("hours", getHourTimeContDesc(flightLeg.durationHour))
                .put("minutes", getMinuteTimeContDesc(flightLeg.durationMinute))
                .put("stops", flightLeg.stopCount)
                .format()
                .toString())
        if(flightSegments != null){
            for (segment in flightSegments) {
                result.append(Phrase.from(context, R.string.flight_detail_flight_duration_card_cont_desc_TEMPLATE).
                        put("departureairport", segment.departureAirportCode).
                        put("arrivalairport", segment.arrivalAirportCode).
                        put("durationhours", getHourTimeContDesc(segment.durationHours)).
                        put("durationmins", getMinuteTimeContDesc(segment.durationMinutes)).format().toString())
                if (segment.layoverDurationHours != 0 || segment.layoverDurationMinutes != 0) {
                    result.append(Phrase.from(context, R.string.flight_detail_layover_duration_card_cont_desc_TEMPLATE).
                            put("layoverhours", getHourTimeContDesc(segment.layoverDurationHours)).
                            put("layovermins", getMinuteTimeContDesc(segment.layoverDurationMinutes)).format().toString())
                }
            }
        }
        result.append(Phrase.from(context.resources.getString(R.string.accessibility_cont_desc_role_button)).format().toString())

        return result.build()
    }

    fun getHourTimeContDesc(hours: Int): CharSequence {
        return Phrase.from(context.resources.getQuantityString(R.plurals.hours_from_now, hours)).put("hours", hours).format().toString()
    }

    fun getMinuteTimeContDesc(minutes: Int): CharSequence {
        return Phrase.from(context.resources.getQuantityString(R.plurals.minutes_from_now, minutes)).put("minutes", minutes).format().toString()
    }
}