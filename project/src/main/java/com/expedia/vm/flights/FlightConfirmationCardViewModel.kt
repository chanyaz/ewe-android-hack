package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.subjects.BehaviorSubject

class FlightConfirmationCardViewModel (private val context: Context, flightLeg: FlightLeg, numberOfGuests: Int) {
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<String>()
    val urlSubject = BehaviorSubject.create<String>()
    val secondaryTitleSubject = BehaviorSubject.create<String>()

    init {
        secondaryTitleSubject.onNext(FlightV2Utils.getDepartureOnDateString(context, flightLeg))
        urlSubject.onNext(FlightV2Utils.getAirlineUrl(flightLeg))
        titleSubject.onNext(getFlightTitle(flightLeg))
        subtitleSubject.onNext(getFlightSubtitle(flightLeg, numberOfGuests))
    }

    private fun getFlightTitle(flightLeg: FlightLeg) : String {
        val arrivalAirportCode = flightLeg.segments?.last()?.arrivalAirportCode ?: ""
        val departureAirportCode = flightLeg.segments?.first()?.departureAirportCode ?: ""
        return context.getString(R.string.SharedItin_Title_Flight_TEMPLATE, departureAirportCode, arrivalAirportCode)
    }

    private fun getFlightSubtitle(flightLeg: FlightLeg, guests: Int): String? {
        val departureDateTime = flightLeg.segments.first().departureTimeRaw
        val departureTime = FlightV2Utils.formatTimeShort(context, departureDateTime ?: "")
        val arrivalTime = FlightV2Utils.formatTimeShort(context, flightLeg.segments.last().arrivalTimeRaw ?: "")
        val stops = FlightV2Utils.getFlightStopString(context, flightLeg)

        return Phrase.from(context.getString(R.string.flight_to_card_crystal_subtitle_TEMPLATE))
                .put("departuretime", departureTime)
                .put("arrivaltime", arrivalTime)
                .put("stops", stops)
                .format().toString()
    }
}