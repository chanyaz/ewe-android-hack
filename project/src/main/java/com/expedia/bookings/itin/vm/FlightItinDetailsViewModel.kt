package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.flightlib.data.Waypoint
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.utils.FormatUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.Days
import rx.subjects.PublishSubject

class FlightItinDetailsViewModel(private val context: Context, private val itinId: String) {

    lateinit var itinCardDataFlight: ItinCardDataFlight
    var itineraryManager: ItineraryManager = ItineraryManager.getInstance()

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

    fun onResume() {
        updateItinCardDataFlight()
        updateToolbar()
        updateLegSummaryWidget()
        updateConfirmationWidget()
        updateBaggageInfoUrl()
        updateBookingInfoWidget()
    }

    @VisibleForTesting
    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlightObservable.onNext(freshItinCardDataFlight)
            itinCardDataFlight = freshItinCardDataFlight
        }
    }

    fun updateConfirmationWidget() {
        val confirmationStatus = itinCardDataFlight.confirmationStatus
        val confirmationNumbers = itinCardDataFlight.getSpannedConfirmationNumbers(context)
        updateConfirmationSubject.onNext(ItinConfirmationViewModel.WidgetParams(confirmationStatus, confirmationNumbers))
    }

    private fun updateToolbar() {
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", itinCardDataFlight.flightLeg.lastWaypoint.airport.mCity ?: "").format().toString()
        val startDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(itinCardDataFlight.startDate).capitalize()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(destinationCity, startDate, !itinCardDataFlight.isSharedItin))
    }

    fun updateBaggageInfoUrl() {
        val url = itinCardDataFlight.baggageInfoUrl
        createBaggageInfoWebviewWidgetSubject.onNext(url);
    }

    fun createOmnitureTrackingValues(): HashMap<String, String?> {
        val duration = calculateTripDuration(itinCardDataFlight)
        val daysUntilTrip = calculateDaysUntilTripStart(itinCardDataFlight)
        val orderAndTripNumbers = buildOrderNumberAndItinNumberString(itinCardDataFlight)
        val tripStartDate = JodaUtils.format(itinCardDataFlight.tripStartDate, "yyyy-MM-dd")
        val tripEndDate = JodaUtils.format(itinCardDataFlight.tripEndDate, "yyyy-MM-dd")
        val productString = buildFlightProductString(itinCardDataFlight)
        val valueMap = HashMap<String, String?>()
        valueMap.put("duration", duration)
        valueMap.put("daysUntilTrip", daysUntilTrip)
        valueMap.put("orderAndTripNumbers", orderAndTripNumbers)
        valueMap.put("tripStartDate", tripStartDate)
        valueMap.put("tripEndDate", tripEndDate)
        valueMap.put("productString", productString)

        return valueMap
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
                if (BuildConfig.DEBUG) {
                    var depart = segment.originWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
                    var arrival =  segment.destinationWaypoint.getDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED)
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
                }
                else {
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
                if (!layoverDuration.isNullOrEmpty()) {
                    createLayoverWidgetSubject.onNext(layoverDuration)
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
    fun updateBookingInfoWidget() {
        val travelerNames  = itinCardDataFlight.travelerFirstAndLastNames
        val isShared = itinCardDataFlight.isSharedItin
        createBookingInfoWidgetSubject.onNext(FlightItinBookingInfoViewModel.WidgetParams(
                travelerNames,
                isShared,
                itinCardDataFlight.detailsUrl
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

    //Helper methods below for calculating Omniture tracking values

    fun calculateTripDuration(trip: ItinCardDataFlight): String? {
        if (trip.tripStartDate != null && trip.tripEndDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            val tripEndDate = trip.tripEndDate.withTimeAtStartOfDay()
            if (tripStartDate == tripEndDate) {
                return "0.0"
            } else {
                return (Days.daysBetween(tripStartDate, tripEndDate).days + 1).toString()
            }
        } else {
            return null
        }
    }

    fun calculateDaysUntilTripStart(trip: ItinCardDataFlight): String? {
        val now = DateTime.now()
        if (trip.tripStartDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            var daysUntil = Days.daysBetween(now, tripStartDate).days
            if (daysUntil >= 1) {
                daysUntil += 1 //this accounts for today
                return daysUntil.toString()
            } else {
                return "0.0"
            }
        } else {
            return null
        }
    }

    fun buildOrderNumberAndItinNumberString(trip: ItinCardDataFlight): String {
        val travelRecordLocator = trip.orderNumber ?: "NA"
        val itinNumber = trip.tripNumber ?: "NA"
        val orderItinstring = StringBuilder()
            orderItinstring.append(travelRecordLocator)
                    .append("|")
                    .append(itinNumber)

        return orderItinstring.toString()
    }

    fun buildFlightProductString(trip: ItinCardDataFlight): String {
        val tripType = getTripType(trip)
        val airlineCode = trip.flightLeg.firstAirlineCode ?: ""
        val productString = StringBuilder()
        productString.append(";Flight:")
                .append(airlineCode)
                .append(":")
                .append(tripType)
                .append(";;")
        return productString.toString()
    }

    fun getTripType(trip: ItinCardDataFlight): String {
        val numLegs = trip.legCount
        if (!trip.isSplitTicket) {
            when (numLegs) {
                1 -> return "OW"
                2 -> return "RT"
                else -> return "MD"
            }
        }
        else {
            return "ST"
        }
    }


}