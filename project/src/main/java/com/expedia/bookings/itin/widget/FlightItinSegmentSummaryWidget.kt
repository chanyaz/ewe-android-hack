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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val airlineLogo: ImageView by bindView(R.id.flight_itin_airline_logo)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val airlineNameAndNumber: TextView by bindView(R.id.flight_itin_airline_name)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val operatedByAirlines: TextView by bindView(R.id.flight_itin_airline_operated_by)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val departureTime: TextView by bindView(R.id.flight_itin_departure_time)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val departureAirport: TextView by bindView(R.id.flight_itin_departure_airport)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val arrivalTime: TextView by bindView(R.id.flight_itin_arrival_time)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val arrivalAirport: TextView by bindView(R.id.flight_itin_arrival_airport)

    var viewModel: FlightItinSegmentSummaryViewModel by notNullAndObservable { vm ->
        vm.createAirlineWidgetSubject.subscribe { params ->
            Picasso.with(context).load(params.airlineLogoURL).placeholder(R.drawable.ic_plane_icon_placeholder_android).into(airlineLogo)
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
    }

    init {
        View.inflate(context, R.layout.widget_flight_itin_segment_summary, this)
    }
}