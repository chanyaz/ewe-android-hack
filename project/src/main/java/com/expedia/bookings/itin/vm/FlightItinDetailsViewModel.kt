package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.flightlib.data.Waypoint
import com.mobiata.flightlib.utils.FormatUtils
import rx.subjects.PublishSubject

class FlightItinDetailsViewModel(private val context: Context, private val itinId: String) {

    lateinit var itinCardDataFlight: ItinCardDataFlight
    var itineraryManager: ItineraryManager = ItineraryManager.getInstance()

    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val updateToolbarSubject: PublishSubject<ItinToolbarViewModel.ToolbarParams> = PublishSubject.create<ItinToolbarViewModel.ToolbarParams>()
    val createSegmentSummaryWidgetsSubject: PublishSubject<FlightItinSegmentSummaryViewModel.SummaryWidgetParams> = PublishSubject.create<FlightItinSegmentSummaryViewModel.SummaryWidgetParams>()
    val clearLegSummaryContainerSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val updateConfirmationSubject: PublishSubject<ItinConfirmationViewModel.WidgetParams> = PublishSubject.create<ItinConfirmationViewModel.WidgetParams>()
    fun onResume() {
        updateItinCardDataFlight()
        updateToolbar()
        updateLegSummaryWidget()
        updateConfirmationWidget()
    }

    @VisibleForTesting
    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlight = freshItinCardDataFlight
        }
    }

    fun updateConfirmationWidget() {
        val confirmationStatus = itinCardDataFlight.confirmationStatus
        val confirmationNumbers = itinCardDataFlight.getSpannedConfirmationNumbers(context)
        updateConfirmationSubject.onNext(ItinConfirmationViewModel.WidgetParams(confirmationStatus, confirmationNumbers))
    }

    private fun updateToolbar() {
        val destinationCity = itinCardDataFlight.flightLeg.lastWaypoint.airport.mCity ?: ""
        val startDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(itinCardDataFlight.startDate).capitalize()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(destinationCity, startDate, !itinCardDataFlight.isSharedItin))
    }

    @VisibleForTesting
    fun updateLegSummaryWidget() {
        clearLegSummaryContainerSubject.onNext(Unit)
        val leg = itinCardDataFlight.flightLeg
        if (leg != null && leg.segmentCount > 0) {
            val segments = leg.segments
            for (segment in segments) {
                val primaryFlightCode = segment.primaryFlightCode
                val operatingFlightCode = segment.operatingFlightCode
                var operatedBy: String? = null
                if ((primaryFlightCode != null) && (operatingFlightCode != null) && (primaryFlightCode != operatingFlightCode)) {
                    if (operatingFlightCode.mAirlineCode != null && operatingFlightCode.mAirlineCode.isNotEmpty()) {
                        val airline = Db.getAirline(operatingFlightCode.mAirlineCode)
                        operatedBy = FormatUtils.formatAirline(airline, context)
                    } else {
                        operatedBy = operatingFlightCode.mAirlineName
                    }
                }

                val (departureTerminal, departureGate) = getTerminalAndGate(segment.originWaypoint, segment.departureTerminal)
                val (arrivalTerminal, arrivalGate) = getTerminalAndGate(segment.destinationWaypoint, segment.arrivalTerminal)

                createSegmentSummaryWidgetsSubject.onNext(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                        leg.airlineLogoURL,
                        FormatUtils.formatFlightNumber(segment, context),
                        operatedBy,
                        segment.originWaypoint.bestSearchDateTime,
                        segment.destinationWaypoint.bestSearchDateTime,
                        segment.originWaypoint.airport.mAirportCode ?: "",
                        segment.originWaypoint.airport.mCity ?: "",
                        segment.destinationWaypoint.airport.mAirportCode ?: "",
                        segment.destinationWaypoint.airport.mCity ?: "",
                        departureTerminal,
                        departureGate,
                        arrivalTerminal,
                        arrivalGate
                ))
            }
        }
    }

    @VisibleForTesting
    fun getTerminalAndGate(wayPoint: Waypoint, fallback: String?): Pair<String?, String?> {
        return if (wayPoint.terminal.isNullOrEmpty() && wayPoint.gate.isNullOrEmpty()) {
            Pair(fallback, null)
        } else if (wayPoint.terminal.isNullOrEmpty()) {
            Pair(null, wayPoint.gate)
        } else if (wayPoint.gate.isNullOrEmpty()) {
            Pair(wayPoint.terminal, null)
        } else {
            Pair(wayPoint.terminal, wayPoint.gate)
        }
    }
}