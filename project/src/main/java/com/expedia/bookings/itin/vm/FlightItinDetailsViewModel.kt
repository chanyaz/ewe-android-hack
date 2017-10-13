package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.flightlib.data.Flight
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateLegSummaryWidget() {
        clearLegSummaryContainerSubject.onNext(Unit)
        val leg = itinCardDataFlight.flightLeg
        if (leg != null && leg.segmentCount > 0) {
            val segments = leg.segments
            for (segment in segments) {
                val primaryFlightCode = segment.primaryFlightCode
                val operatingFlightCode = segment.operatingFlightCode
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
                if (segment.hasSeats()) {
                    confirmSeats = context.getString(R.string.confirm_seat_selection)
                }
                seats = getSeatString(segment)
                if (segment.hasCabinCode()) {
                    cabinCodeBuilder.append(segment.cabinCode)
                }
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
                        seats,
                        cabinCodeBuilder.toString(),
                        confirmSeats
                ))
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getSeatString(segment: Flight): String {
        if(segment.hasSeats()) {
            return segment.getFirstSixSeats(segment.assignedSeats)
        } else {
            return if (segment.isSeatMapAvailable) {
                context.getString(R.string.select_seat_prompt)

            } else {
                context.getString(R.string.seat_selection_not_available)
            }
        }
    }
}