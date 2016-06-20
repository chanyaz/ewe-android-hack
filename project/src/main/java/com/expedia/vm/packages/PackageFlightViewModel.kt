package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.squareup.phrase.Phrase

class PackageFlightViewModel(private val context: Context, private val flightLeg: FlightLeg) {
    val resources = context.resources
    val flightTime = PackageFlightUtils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
    val asscesibleFlightTime = PackageFlightUtils.getAccessibleDepartArrivalTime(context, flightLeg)
    val price = if (flightLeg.packageOfferModel.price.deltaPositive) ("+" + flightLeg.packageOfferModel.price.differentialPriceFormatted) else flightLeg.packageOfferModel.price.differentialPriceFormatted
    val airline = PackageFlightUtils.getDistinctiveAirline(flightLeg.airlines)
    val duration = PackageFlightUtils.getFlightDurationStopString(context, flightLeg)
    val layover = flightLeg
    var flightSegments = flightLeg.flightSegments

    var contentDescription = getFlightContentDesc()

    fun getFlightContentDesc(): CharSequence {
        var result = SpannableBuilder()

        result.append(Phrase.from(context, R.string.flight_detail_card_cont_desc_TEMPLATE)
                .put("time", asscesibleFlightTime)
                .put("pricedifference", price)
                .put("airline", PackageFlightUtils.getAirlinesList(airline))
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
