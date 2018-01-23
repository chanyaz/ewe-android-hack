package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightItinDetailsResponse
import java.util.Locale

class ConfirmationToolbarViewModel(val context: Context) {

    lateinit var tripId: String

    fun bindTripId(tripId: String) {
        this.tripId = tripId
    }

    fun getShareMessage(itinDetailsResponse: AbstractItinDetailsResponse): String {
        var shareText: String

        val flightItinDetailsResponse = itinDetailsResponse as FlightItinDetailsResponse
        val departureCity = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.firstOrNull()?.segments?.first()?.departureLocation?.city
        val arrivalCity = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.firstOrNull()?.segments?.last()?.arrivalLocation?.city
        val outboundSharableDetailsURL = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.firstOrNull()?.sharableFlightLegURL?.replace("/api/", "/m/")
        val departureDate = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.firstOrNull()?.segments?.firstOrNull()?.departureTime?.localizedShortDate
        if (Db.getFlightSearchParams().isRoundTrip()) {
            val segmentsLength = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.getOrNull(1)?.segments?.size ?: 1
            val arrivalDate = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.getOrNull(1)?.segments?.getOrNull(segmentsLength - 1)?.arrivalTime?.localizedShortDate
            val inboundSharableDetailsURL = flightItinDetailsResponse.responseData.flights.firstOrNull()?.legs?.getOrNull(1)?.sharableFlightLegURL?.replace("/api/", "/m/")
            if (Locale.US == Locale.getDefault()) {
                val template = context.getString(R.string.share_msg_template_roundtrip_flight)
                shareText = String.format(template, departureCity, arrivalCity, departureDate, arrivalDate, outboundSharableDetailsURL, inboundSharableDetailsURL)
            } else {
                shareText = outboundSharableDetailsURL + "\n" + inboundSharableDetailsURL
            }
        } else {
            if (Locale.US == Locale.getDefault()) {
                val template = context.getString(R.string.share_msg_template_short_flight)
                shareText = String.format(template, arrivalCity, departureDate, outboundSharableDetailsURL)
            } else {
                shareText = outboundSharableDetailsURL as String
            }
        }

        return shareText
    }
}
