package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.subjects.PublishSubject

class FlightOverviewSummaryViewModel(val context: Context) {

    val params = PublishSubject.create<FlightSearchParams>()
    val outboundFlightTitle = PublishSubject.create<String>()
    val inboundFlightTitle = PublishSubject.create<String>()

    init {
        params.subscribe { params ->
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val departureAirport = params.departureAirport.hierarchyInfo!!.airport!!.airportCode
            val arrivalAirport = params.arrivalAirport.hierarchyInfo!!.airport!!.airportCode

            outboundFlightTitle.onNext(Phrase.from(context, R.string.flight_overview_header_TEMPLATE)
                    .put("departureairportcode", departureAirport)
                    .put("arrivalairportcode", arrivalAirport)
                    .put("dateoftravelling", DateFormatUtils.formatLocalDateToShortDayAndDate(params?.departureDate?.toString(formatter)))
                    .format().toString())

            inboundFlightTitle.onNext(
                    if (params?.returnDate != null) {
                        Phrase.from(context, R.string.flight_overview_header_TEMPLATE)
                                .put("departureairportcode", arrivalAirport)
                                .put("arrivalairportcode", departureAirport)
                                .put("dateoftravelling", DateFormatUtils.formatLocalDateToShortDayAndDate(params?.returnDate?.toString(formatter)))
                                .format().toString()
                    } else {
                        ""
                    })
        }
    }
}
