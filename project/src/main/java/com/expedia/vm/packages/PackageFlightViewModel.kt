package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import com.squareup.phrase.Phrase

class PackageFlightViewModel(private val context: Context, private val flightLeg: FlightLeg) {
    val resources = context.resources
    val flightTime = PackageFlightUtils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
    val price = if (flightLeg.packageOfferModel.price.deltaPositive) ("+" + flightLeg.packageOfferModel.price.differentialPriceFormatted) else flightLeg.packageOfferModel.price.differentialPriceFormatted
    val airline = PackageFlightUtils.getDistinctiveAirline(flightLeg.airlines)
    val duration = PackageFlightUtils.getFlightDurationStopString(context, flightLeg)
    val layover = flightLeg

    var contentDescription = Phrase.from(context, R.string.flight_detail_card_content_description)
            .put("time", flightTime)
            .put("pricedifference", price)
            .put("airline", PackageFlightUtils.getAirlinesList(airline))
            .put("hours", flightLeg.durationHour)
            .put("minutes", flightLeg.durationMinute)
            .format()
            .toString()
}
