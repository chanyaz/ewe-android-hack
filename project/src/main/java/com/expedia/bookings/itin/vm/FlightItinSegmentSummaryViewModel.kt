package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
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
            val arrivalAirportCity: String,
            var departureTerminal: String?,
            var departureGate: String?,
            var arrivalTerminal: String?,
            var arrivalGate: String?,
            val seats: String,
            val cabinCode: String,
            val seatConfirmation: String?,
            val redEyeDays: String?
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

    data class RedEyeParams(
            val departureRedEye: String?,
            val arrivalRedEye: String?,
            val redEyeDays: String?
    )

    data class TerminalGateParams(
            val departureTerminalGate: String?,
            val arrivalTerminalGate: String?
    )

    data class SeatingWidgetParams(
            val seats: String,
            val cabinCode: String,
            val seatConfirmation: String?
    )

    val createAirlineWidgetSubject: PublishSubject<AirlineWidgetParams> = PublishSubject.create<AirlineWidgetParams>()
    val createTimingWidgetSubject: PublishSubject<TimingWidgetParams> = PublishSubject.create<TimingWidgetParams>()
    val updateTerminalGateSubject: PublishSubject<TerminalGateParams> = PublishSubject.create<TerminalGateParams>()
    val createSeatingWidgetSubject: PublishSubject<SeatingWidgetParams> = PublishSubject.create<SeatingWidgetParams>()
    val createRedEyeWidgetSubject: PublishSubject<RedEyeParams> = PublishSubject.create<RedEyeParams>()

    fun updateWidget(summaryWidgetParams: SummaryWidgetParams) {
        val logoUrl = summaryWidgetParams.airlineLogoURL
        var operatedBy = summaryWidgetParams.operatedByAirlines
        var arrivalRedEye: String? = null
        var departureRedEye: String? = null
        if(!summaryWidgetParams.redEyeDays.isNullOrEmpty()) {
            departureRedEye = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(summaryWidgetParams.departureTime)
            arrivalRedEye = Phrase.from(context, R.string.itin_flight_summary_arrives_on_TEMPLATE).
                    put("date", LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(summaryWidgetParams.arrivalTime)).format().toString()
        }
        if (operatedBy != null) {
            operatedBy = Phrase.from(context, R.string.itin_flight_summary_operated_by_TEMPLATE).put("operatedby", operatedBy).format().toString()
        }
        createAirlineWidgetSubject.onNext(AirlineWidgetParams(
                logoUrl,
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

        createRedEyeWidgetSubject.onNext(RedEyeParams(
                departureRedEye,
                arrivalRedEye,
                summaryWidgetParams.redEyeDays
        ))


        val departureTerminalGate = getTerminalGateString(summaryWidgetParams.departureTerminal, summaryWidgetParams.departureGate)
        val arrivalTerminalGate = getTerminalGateString(summaryWidgetParams.arrivalTerminal, summaryWidgetParams.arrivalGate)
        updateTerminalGateSubject.onNext(TerminalGateParams(departureTerminalGate, arrivalTerminalGate))

        createSeatingWidgetSubject.onNext(SeatingWidgetParams(
                summaryWidgetParams.seats,
                summaryWidgetParams.cabinCode,
                summaryWidgetParams.seatConfirmation
        ))
    }

    @VisibleForTesting
    fun getTerminalGateString(terminal: String?, gate: String?): String? {
        return if (terminal.isNullOrEmpty() && gate.isNullOrEmpty()) {
            null
        } else if (terminal.isNullOrEmpty()) {
            Phrase.from(context, R.string.itin_flight_summary_gate_TEMPLATE).put("gate", gate).format().toString()
        } else if (gate.isNullOrEmpty()) {
            Phrase.from(context, R.string.itin_flight_summary_terminal_TEMPLATE).put("terminal", terminal).format().toString()
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder.append(Phrase.from(context, R.string.itin_flight_summary_terminal_TEMPLATE).put("terminal", terminal).format().toString())
            stringBuilder.append(", ")
            stringBuilder.append(Phrase.from(context, R.string.itin_flight_summary_gate_TEMPLATE).put("gate", gate).format().toString())
            stringBuilder.toString()
        }
    }
}
