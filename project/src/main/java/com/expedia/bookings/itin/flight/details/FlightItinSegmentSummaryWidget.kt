package com.expedia.bookings.itin.flight.details

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.squareup.picasso.Picasso

class FlightItinSegmentSummaryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    @VisibleForTesting
    val airlineLogo: ImageView by bindView(R.id.flight_itin_airline_logo)
    @VisibleForTesting
    val airlineNameAndNumber: TextView by bindView(R.id.flight_itin_airline_name)
    @VisibleForTesting
    val operatedByAirlines: TextView by bindView(R.id.flight_itin_airline_operated_by)

    @VisibleForTesting
    val departureTime: TextView by bindView(R.id.flight_itin_departure_time)
    @VisibleForTesting
    val departureAirport: TextView by bindView(R.id.flight_itin_departure_airport)
    @VisibleForTesting
    val arrivalTime: TextView by bindView(R.id.flight_itin_arrival_time)
    @VisibleForTesting
    val arrivalAirport: TextView by bindView(R.id.flight_itin_arrival_airport)
    @VisibleForTesting
    val departureTerminalGate: TextView by bindView(R.id.flight_itin_departure_terminal_gate)
    @VisibleForTesting
    val arrivalTerminalGate: TextView by bindView(R.id.flight_itin_arrival_terminal_gate)

    @VisibleForTesting
    val seats: TextView by bindView(R.id.flight_itin_seating)
    @VisibleForTesting
    val cabin: TextView by bindView(R.id.flight_itin_cabin)
    @VisibleForTesting
    val seatConfirmation: TextView by bindView(R.id.flight_seating_class)

    @VisibleForTesting
    val arrivalRedEye: TextView by bindView(R.id.arrival_red_eye)
    @VisibleForTesting
    val departureRedEye: TextView by bindView(R.id.departure_red_eye)
    @VisibleForTesting
    val redEyeDays: TextView by bindView(R.id.red_eye_days)

    @VisibleForTesting
    val flightStatusIndicatorContainer: LinearLayout by bindView(R.id.flight_status_indicator_container)
    @VisibleForTesting
    val flightStatusIndicatorText: TextView by bindView(R.id.flight_status_indicator_text)

    @VisibleForTesting
    val newDepartureDetailsContainer: LinearLayout by bindView(R.id.flight_itin_new_departure_details_container)
    @VisibleForTesting
    val newDepartureTimeText: TextView by bindView(R.id.flight_itin_new_departure_time)
    @VisibleForTesting
    val newArrivalDetailsContainer: LinearLayout by bindView(R.id.flight_itin_new_arrival_details_container)
    @VisibleForTesting
    val newArrivalTimeText: TextView by bindView(R.id.flight_itin_new_arrival_time)
    val flightItinArrow: ImageView by bindView(R.id.flight_itin_to_arrow)

    var viewModel: FlightItinSegmentSummaryViewModel by notNullAndObservable { vm ->
        vm.createAirlineWidgetSubject.subscribe { params ->
            if (!params.airlineLogoURL.isNullOrEmpty()) {
                Picasso.with(context).load(params.airlineLogoURL).into(airlineLogo)
            } else {
                airlineLogo.setImageResource(R.drawable.ic_plane_icon_placeholder_android)
            }
            airlineNameAndNumber.text = params.airlineName
            if (params.operatedByAirlines != null) {
                operatedByAirlines.visibility = View.VISIBLE
                operatedByAirlines.text = params.operatedByAirlines
            }
        }

        vm.createTimingWidgetSubject.subscribe { params ->
            departureTime.text = params.departureTime
            arrivalTime.text = params.arrivalTime
            departureAirport.text = params.departureAirport
            arrivalAirport.text = params.arrivalAirport
        }

        vm.createRedEyeWidgetSubject.subscribe { params ->
            if (!params.redEyeDays.isNullOrEmpty()) {
                redEyeDays.visibility = View.VISIBLE
                redEyeDays.text = params.redEyeDays
            }
            if (!params.arrivalRedEye.isNullOrEmpty()) {
                arrivalRedEye.visibility = View.VISIBLE
                arrivalRedEye.text = params.arrivalRedEye
            }
            if (!params.departureRedEye.isNullOrEmpty()) {
                departureRedEye.visibility = View.VISIBLE
                departureRedEye.text = params.departureRedEye
            }
        }

        vm.updateTerminalGateSubject.subscribe { params ->
            if (!params.departureTerminalGate.isNullOrEmpty()) {
                departureTerminalGate.visibility = View.VISIBLE
                departureTerminalGate.text = params.departureTerminalGate
            }
            if (!params.arrivalTerminalGate.isNullOrEmpty()) {
                arrivalTerminalGate.visibility = View.VISIBLE
                arrivalTerminalGate.text = params.arrivalTerminalGate
            }
        }

        vm.createSeatingWidgetSubject.subscribe { params ->
            seats.text = params.seats
            cabin.text = params.cabinCode
            if (params.seatConfirmation != null) {
                seatConfirmation.visibility = View.VISIBLE
                seatConfirmation.text = params.seatConfirmation
            }
        }

        vm.updateFlightStatusSubject.subscribe { flightStatsParams ->
            flightStatusIndicatorContainer.visibility = View.VISIBLE
            flightStatusIndicatorContainer.background = ContextCompat.getDrawable(context, flightStatsParams.indicatorContainerBackground)
            flightStatusIndicatorText.text = flightStatsParams.flightStatusText
            flightStatusIndicatorText.contentDescription = flightStatsParams.flightStatusTextContDesc
            if (!flightStatsParams.newArrivalTimeText.isNullOrEmpty() && !flightStatsParams.newDepartureTimeText.isNullOrEmpty()) {
                newDepartureDetailsContainer.visibility = View.VISIBLE
                newArrivalDetailsContainer.visibility = View.VISIBLE
                flightItinArrow.visibility = View.GONE
                newDepartureTimeText.text = flightStatsParams.newDepartureTimeText
                newArrivalTimeText.text = flightStatsParams.newArrivalTimeText
                newDepartureTimeText.setTextColor(ContextCompat.getColor(context, flightStatsParams.indicatorTextColor))
                newArrivalTimeText.setTextColor(ContextCompat.getColor(context, flightStatsParams.indicatorTextColor))
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_flight_itin_segment_summary, this)
    }
}
