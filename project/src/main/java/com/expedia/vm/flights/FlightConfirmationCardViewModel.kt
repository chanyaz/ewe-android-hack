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

    init {
        titleSubject.onNext(getFlightTitle(flightLeg))
        subtitleSubject.onNext(getFlightSubtitle(flightLeg.departureDateTimeISO, numberOfGuests))
    }

    private fun getFlightTitle(flightLeg: FlightLeg) : String {
        val airportCode = flightLeg.segments?.last()?.arrivalAirportCode ?: ""
        val airportCity = flightLeg.segments?.last()?.arrivalAirportAddress?.city ?: ""

        return Phrase.from(context.getString(R.string.flight_to_card_TEMPLATE))
                .put("airportcode", airportCode)
                .put("airportcity", airportCity)
                .format().toString()
    }

     fun getFlightSubtitle(departureDateTime: String?, guests: Int): String? {
         val localDepartureDateDate = DateUtils.localDateToMMMd(DateTime.parse(departureDateTime).toLocalDate())
         val departureTime = FlightV2Utils.formatTimeShort(context, departureDateTime ?: "")
         val numberOfGuests = StrUtils.formatTravelerString(context, guests)

         return context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, localDepartureDateDate, departureTime, numberOfGuests)
    }
}
