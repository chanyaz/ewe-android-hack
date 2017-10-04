package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.subjects.PublishSubject

class FlightItinSegmentSummaryViewModel(private val context: Context) {
    data class SummaryWidgetParams(
            val airlineLogoURL: String?,
            val airlineName: String,
            val operatedByAirlines: String?,
            val departureTime: DateTime,
            val arrivalTime: DateTime,
            val departureAirportCode: String,
            val departureAirportCity: String,
            val arrivalAirportCode: String,
            val arrivalAirportCity: String
    )

    data class AirlineWidgetParams(
            val airlineLogoURL: String?,
            val airlineName: String,
            val operatedByAirlines: String?
    )

    data class TimingWidgetParams(
            val departureTime: String,
            val arrivalTime: String,
            val departureAirport: String,
            val arrivalAirport: String
    )

    val createAirlineWidgetSubject: PublishSubject<AirlineWidgetParams> = PublishSubject.create<AirlineWidgetParams>()
    val createTimingWidgetSubject: PublishSubject<TimingWidgetParams> = PublishSubject.create<TimingWidgetParams>()

    fun updateWidget(summaryWidgetParams: SummaryWidgetParams) {
        val logoUrl = summaryWidgetParams.airlineLogoURL
        var operatedBy = summaryWidgetParams.operatedByAirlines
        if (operatedBy != null) {
            operatedBy = Phrase.from(context, R.string.itin_flight_summary_operated_by_TEMPLATE).put("operatedby", operatedBy).format().toString()
        }
        createAirlineWidgetSubject.onNext(AirlineWidgetParams(
                if (logoUrl != null && logoUrl.isNotEmpty()) logoUrl else null,
                summaryWidgetParams.airlineName,
                operatedBy
        ))

        createTimingWidgetSubject.onNext(TimingWidgetParams(
                LocaleBasedDateFormatUtils.dateTimeTohmma(summaryWidgetParams.departureTime).toLowerCase(),
                LocaleBasedDateFormatUtils.dateTimeTohmma(summaryWidgetParams.arrivalTime).toLowerCase(),
                Phrase.from(context, R.string.itin_flight_summary_airport_name_code_TEMPLATE)
                        .put("city", summaryWidgetParams.departureAirportCity)
                        .put("code", summaryWidgetParams.departureAirportCode).format().toString(),
                Phrase.from(context, R.string.itin_flight_summary_airport_name_code_TEMPLATE)
                        .put("city", summaryWidgetParams.arrivalAirportCity)
                        .put("code", summaryWidgetParams.arrivalAirportCode).format().toString()
        ))
    }
}