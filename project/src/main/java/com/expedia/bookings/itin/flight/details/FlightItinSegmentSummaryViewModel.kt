package com.expedia.bookings.itin.flight.details

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateRangeUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.flightlib.data.Flight
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

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
        val redEyeDays: String?,
        val flightStatus: String,
        val estimatedGateDepartureTime: DateTime?,
        val estimatedGateArrivalTime: DateTime?
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

    data class FlightStatsParams(
        val indicatorContainerBackground: Int,
        val flightStatusText: String,
        val flightStatusTextContDesc: String,
        val indicatorTextColor: Int,
        val newDepartureTimeText: String?,
        val newArrivalTimeText: String?
    )

    val createAirlineWidgetSubject: PublishSubject<AirlineWidgetParams> = PublishSubject.create<AirlineWidgetParams>()
    val createTimingWidgetSubject: PublishSubject<TimingWidgetParams> = PublishSubject.create<TimingWidgetParams>()
    val updateTerminalGateSubject: PublishSubject<TerminalGateParams> = PublishSubject.create<TerminalGateParams>()
    val createSeatingWidgetSubject: PublishSubject<SeatingWidgetParams> = PublishSubject.create<SeatingWidgetParams>()
    val createRedEyeWidgetSubject: PublishSubject<RedEyeParams> = PublishSubject.create<RedEyeParams>()
    val updateFlightStatusSubject: PublishSubject<FlightStatsParams> = PublishSubject.create<FlightStatsParams>()

    fun updateSegmentInformation(summaryWidgetParams: SummaryWidgetParams) {
        val logoUrl = summaryWidgetParams.airlineLogoURL
        var operatedBy = summaryWidgetParams.operatedByAirlines
        var arrivalRedEye: String? = null
        var departureRedEye: String? = null
        if (!summaryWidgetParams.redEyeDays.isNullOrEmpty()) {
            departureRedEye = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(summaryWidgetParams.departureTime)
            arrivalRedEye = Phrase.from(context, R.string.itin_flight_summary_arrives_on_TEMPLATE)
                    .put("date", LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(summaryWidgetParams.arrivalTime)).format().toString()
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

        updateFlightStatus(summaryWidgetParams.flightStatus, summaryWidgetParams.departureTime, summaryWidgetParams.estimatedGateDepartureTime, summaryWidgetParams.estimatedGateArrivalTime)
    }

    @VisibleForTesting
    fun updateFlightStatus(flightStatus: String, scheduledDepartureTime: DateTime, estimatedGateDepartureTime: DateTime?, estimatedGateArrivalTime: DateTime?) {
        if (flightStatus == Flight.STATUS_CANCELLED) {
            val flightIndicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_cancelled)
            updateFlightStatusSubject.onNext(FlightStatsParams(
                    R.drawable.flight_status_indicator_error_background,
                    flightIndicatorText,
                    flightIndicatorText,
                    R.color.itin_status_indicator_error,
                    null,
                    null
            ))
        } else if (estimatedGateDepartureTime != null && estimatedGateArrivalTime != null) {
            val departureDelay = DateRangeUtils.getMinutesBetween(scheduledDepartureTime, estimatedGateDepartureTime)
            when {
                departureDelay < 0 -> {
                    val flightIndicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_early_departure)
                    updateFlightStatusSubject.onNext(FlightStatsParams(
                            R.drawable.flight_status_indicator_success_background,
                            flightIndicatorText,
                            flightIndicatorText,
                            R.color.itin_status_indicator_success,
                            LocaleBasedDateFormatUtils.dateTimeTohmma(estimatedGateDepartureTime).toLowerCase(),
                            LocaleBasedDateFormatUtils.dateTimeTohmma(estimatedGateArrivalTime).toLowerCase()
                    ))
                }
                departureDelay > 0 -> {
                    val delayText = Phrase.from(context, R.string.itin_flight_summary_status_indicator_text_delayed_by_TEMPLATE)
                            .put("duration", DateRangeUtils.formatDurationDaysHoursMinutes(context, departureDelay)).format().toString()
                    val delayTextContDesc = Phrase.from(context, R.string.itin_flight_summary_status_indicator_text_delayed_by_TEMPLATE)
                            .put("duration", DateRangeUtils.getDurationContDescDaysHoursMins(context, departureDelay)).format().toString()
                    updateFlightStatusSubject.onNext(FlightStatsParams(
                            R.drawable.flight_status_indicator_error_background,
                            delayText,
                            delayTextContDesc,
                            R.color.itin_status_indicator_error,
                            LocaleBasedDateFormatUtils.dateTimeTohmma(estimatedGateDepartureTime).toLowerCase(),
                            LocaleBasedDateFormatUtils.dateTimeTohmma(estimatedGateArrivalTime).toLowerCase()
                    ))
                }
                else -> {
                    val flightIndicatorText = context.resources.getString(R.string.itin_flight_summary_status_indicator_text_on_time)
                    updateFlightStatusSubject.onNext(FlightStatsParams(
                            R.drawable.flight_status_indicator_success_background,
                            flightIndicatorText,
                            flightIndicatorText,
                            R.color.itin_status_indicator_success,
                            null,
                            null
                    ))
                }
            }
        }
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
