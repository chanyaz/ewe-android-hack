package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinSegmentSummaryViewModel
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
            if(params.seatConfirmation != null) {
                seatConfirmation.visibility = View.VISIBLE
                seatConfirmation.text = params.seatConfirmation
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_flight_itin_segment_summary, this)
    }
}
