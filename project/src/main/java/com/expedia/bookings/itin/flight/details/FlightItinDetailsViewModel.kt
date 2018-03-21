package com.expedia.bookings.itin.flight.details

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.itin.common.ItinConfirmationViewModel
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Waypoint
import com.mobiata.flightlib.utils.FormatUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

class FlightItinDetailsViewModel(private val context: Context, private val itinId: String) {

    var itineraryManager: ItineraryManagerInterface = ItineraryManager.getInstance()
    private var trackingFired = false

    val itinCardDataFlightObservable = PublishSubject.create<ItinCardDataFlight>()
    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val updateToolbarSubject: PublishSubject<ItinToolbarViewModel.ToolbarParams> = PublishSubject.create<ItinToolbarViewModel.ToolbarParams>()
    val createSegmentSummaryWidgetsSubject: PublishSubject<FlightItinSegmentSummaryViewModel.SummaryWidgetParams> = PublishSubject.create<FlightItinSegmentSummaryViewModel.SummaryWidgetParams>()
    val createLayoverWidgetSubject: PublishSubject<String> = PublishSubject.create<String>()
    val createTotalDurationWidgetSubject: PublishSubject<String> = PublishSubject.create<String>()
    val clearLegSummaryContainerSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val updateConfirmationSubject: PublishSubject<ItinConfirmationViewModel.WidgetParams> = PublishSubject.create<ItinConfirmationViewModel.WidgetParams>()
    val createBaggageInfoWebviewWidgetSubject: PublishSubject<String> = PublishSubject.create<String>()
    val createBookingInfoWidgetSubject: PublishSubject<FlightItinBookingInfoViewModel.WidgetParams> = PublishSubject.create<FlightItinBookingInfoViewModel.WidgetParams>()

    init {
        itinCardDataFlightObservable.subscribe {
            updateToolbar(it)
            updateLegSummaryWidget(it)
            updateConfirmationWidget(it)
            updateBaggageInfoUrl(it)
            updateBookingInfoWidget(it)
            trackOmniture(it)
        }
    }

    private fun trackOmniture(dataFlight: ItinCardDataFlight) {
        if (!trackingFired) {
            OmnitureTracking.trackItinFlight(createOmnitureTrackingValues(dataFlight))
            trackingFired = true
        }
    }

    fun onResume() {
        updateItinCardDataFlight()
    }

    @VisibleForTesting
    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlightObservable.onNext(freshItinCardDataFlight)
        }
    }

    fun updateConfirmationWidget(dataFlight: ItinCardDataFlight) {
        val isShared = dataFlight.isSharedItin
        val confirmationStatus = dataFlight.confirmationStatus
        val confirmationNumbers = dataFlight.getSpannedConfirmationNumbers(context)
        updateConfirmationSubject.onNext(ItinConfirmationViewModel.WidgetParams(confirmationStatus, confirmationNumbers, isShared))
    }

    private fun updateToolbar(dataFlight: ItinCardDataFlight) {
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                .put("destination", dataFlight.flightLeg.lastWaypoint?.airport?.mCity ?: "").format().toString()
        val startDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(dataFlight.startDate).capitalize()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(destinationCity, startDate, !dataFlight.isSharedItin))
    }

    fun updateBaggageInfoUrl(dataFlight: ItinCardDataFlight) {
        val url = dataFlight.baggageInfoUrl
        if (url != null) {
            createBaggageInfoWebviewWidgetSubject.onNext(url)
        } else {
            createBaggageInfoWebviewWidgetSubject.onNext("")
        }
    }

    fun createOmnitureTrackingValues(dataFlight: ItinCardDataFlight): HashMap<String, String?> {
        return ItinOmnitureUtils.createOmnitureTrackingValues(dataFlight)
    }

    @VisibleForTesting
    fun updateLegSummaryWidget(dataFlight: ItinCardDataFlight) {
        clearLegSummaryContainerSubject.onNext(Unit)
        val leg = dataFlight.flightLeg
        if (leg != null && leg.segmentCount > 0) {
            val segments = leg.segments
            for (segment in segments) {
                val primaryFlightCode = segment.primaryFlightCode
                val operatingFlightCode = segment.operatingFlightCode
                var redEyeDaysSB: StringBuilder?
                var redEyeDays: String? = null
                var operatedBy: String? = null
                var seats: String?
                var confirmSeats: String? = null
                val cabinCodeBuilder = StringBuilder(" • ")
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

                if (segment.hasSeats()) {
                    confirmSeats = context.getString(R.string.confirm_seat_selection)
                }
                seats = getSeatString(segment)
                if (segment.hasCabinCode()) {
                    cabinCodeBuilder.append(segment.cabinCode)
                }

                if (segment.hasRedEye()) {
                    if (segment.daySpan() > 0) {
                        redEyeDaysSB = StringBuilder("+")
                    } else {
                        redEyeDaysSB = StringBuilder()
                    }
                    redEyeDaysSB.append(segment.daySpan())
                    redEyeDays = redEyeDaysSB.toString()
                }
                if (dataFlight.id == "flightMock") {
                    var depart = segment.originWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
                    var arrival = segment.destinationWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
                    when (segment.mFlightHistoryId) {
                        -91 -> segment.mStatusCode = Flight.STATUS_CANCELLED
                        -93 -> depart = depart.plusMinutes(20)
                        -94 -> depart = depart.plusMinutes(-20)
                        -1 -> {
                            depart = null
                            arrival = null
                        }
                    }

                    createSegmentSummaryWidgetsSubject.onNext(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                            leg.airlineLogoURL,
                            FormatUtils.formatFlightNumber(segment, context),
                            operatedBy,
                            segment.originWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED),
                            segment.destinationWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED),
                            segment.originWaypoint.airport.mAirportCode ?: "",
                            segment.originWaypoint.airport.mCity ?: "",
                            segment.destinationWaypoint.airport.mAirportCode ?: "",
                            segment.destinationWaypoint.airport.mCity ?: "",
                            departureTerminal,
                            departureGate,
                            arrivalTerminal,
                            arrivalGate,
                            seats,
                            cabinCodeBuilder.toString(),
                            confirmSeats,
                            redEyeDays,
                            segment.mStatusCode,
                            depart,
                            arrival
                    ))
                } else {
                    createSegmentSummaryWidgetsSubject.onNext(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                            leg.airlineLogoURL,
                            FormatUtils.formatFlightNumber(segment, context),
                            operatedBy,
                            getScheduledDepartureTime(segment),
                            getScheduledArrivalTime(segment),
                            segment.originWaypoint.airport.mAirportCode ?: "",
                            segment.originWaypoint.airport.mCity ?: "",
                            segment.destinationWaypoint.airport.mAirportCode ?: "",
                            segment.destinationWaypoint.airport.mCity ?: "",
                            departureTerminal,
                            departureGate,
                            arrivalTerminal,
                            arrivalGate,
                            seats,
                            cabinCodeBuilder.toString(),
                            confirmSeats,
                            redEyeDays,
                            segment.mStatusCode,
                            getEstimatedGateDepartureTime(segment),
                            getEstimatedGateArrivalTime(segment)
                    ))
                }

                val layoverDuration = segment.layoverDuration
                layoverDuration?.let {
                    if (!it.isEmpty()) {
                        createLayoverWidgetSubject.onNext(it)
                    }
                }
            }

            val totalDuration = leg.legDuration
            if (!totalDuration.isNullOrEmpty()) {
                createTotalDurationWidgetSubject.onNext(totalDuration)
            }
        }
    }

    //return time from flightstats if available, else return time from trips API
    @VisibleForTesting
    fun getScheduledDepartureTime(flight: Flight): DateTime {
        if (isDataAvailableFromFlightStats(flight)) {
            return flight.originWaypoint.getDateTime(Waypoint.POSITION_GATE, Waypoint.ACCURACY_SCHEDULED)
        }
        return flight.originWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
    }

    //return time from flightstats if available, else return time from trips API
    @VisibleForTesting
    fun getScheduledArrivalTime(flight: Flight): DateTime {
        if (isDataAvailableFromFlightStats(flight)) {
            return flight.destinationWaypoint.getDateTime(Waypoint.POSITION_GATE, Waypoint.ACCURACY_SCHEDULED)
        }
        return flight.destinationWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
    }

    //return time from flightstats if available, else return null
    @VisibleForTesting
    fun getEstimatedGateDepartureTime(flight: Flight): DateTime? {
        return if (isDataAvailableFromFlightStats(flight)) flight.originWaypoint.getDateTime(Waypoint.POSITION_GATE, Waypoint.ACCURACY_ESTIMATED) else null
    }

    //return time from flightstats if available, else return null
    @VisibleForTesting
    fun getEstimatedGateArrivalTime(flight: Flight): DateTime? {
        return if (isDataAvailableFromFlightStats(flight)) flight.destinationWaypoint.getDateTime(Waypoint.POSITION_GATE, Waypoint.ACCURACY_ESTIMATED) else null
    }

    @VisibleForTesting
    fun isDataAvailableFromFlightStats(flight: Flight): Boolean {
        return (flight.mFlightHistoryId != -1)
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

    @VisibleForTesting
    fun updateBookingInfoWidget(dataFlight: ItinCardDataFlight) {
        val travelerNames = dataFlight.travelersFullName
        val isShared = dataFlight.isSharedItin
        createBookingInfoWidgetSubject.onNext(FlightItinBookingInfoViewModel.WidgetParams(
                travelerNames,
                isShared,
                dataFlight.detailsUrl,
                dataFlight.id,
                dataFlight.tripNumber
        ))
    }

    @VisibleForTesting
    fun getSeatString(segment: Flight): String {
        return when {
            segment.hasSeats() -> segment.getFirstSixSeats(segment.assignedSeats)
            segment.isSeatMapAvailable -> context.getString(R.string.select_seat_prompt)
            else -> context.getString(R.string.seat_selection_not_available)
        }
    }
}
