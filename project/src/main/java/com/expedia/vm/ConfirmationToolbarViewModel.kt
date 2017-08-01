package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import io.reactivex.subjects.PublishSubject
import java.util.Locale

class ConfirmationToolbarViewModel(val context: Context) {

    val tripId: String get() = checkoutResponse.newTrip?.tripId as String

    private lateinit var checkoutResponse: FlightCheckoutResponse

    fun bindCheckoutResponseData(checkoutResponse: FlightCheckoutResponse) {
        this.checkoutResponse = checkoutResponse
    }

    fun getShareMessage(itinDetailsResponse: AbstractItinDetailsResponse): String {
        var shareText = ""

        val flightItinDetailsResponse = itinDetailsResponse as FlightItinDetailsResponse
        val departureCity = checkoutResponse.getFirstFlightFirstSegment().departureAirportAddress.city
        val arrivalCity = checkoutResponse.getFirstFlightLastSegment().arrivalAirportAddress.city
        val outboundSharableDetailsURL = flightItinDetailsResponse.getOutboundSharableDetailsURL()
        val departureDate = flightItinDetailsResponse.getOutboundDepartureDate()
        if (Db.getFlightSearchParams().isRoundTrip()) {
            val arrivalDate = flightItinDetailsResponse.getInboundArrivalDate()
            val inboundSharableDetailsURL = flightItinDetailsResponse.getInboundSharableDetailsURL()
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