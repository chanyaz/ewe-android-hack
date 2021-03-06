package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.squareup.phrase.Phrase

public class FlightSegmentBreakdownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: FlightSegmentBreakdownViewModel by notNullAndObservable { vm ->
        vm.addSegmentRowsObserver.subscribe {
            linearLayout.removeAllViews()
            for (segmentbreakdown in it) {
                linearLayout.addView(createSegmentRow(segmentbreakdown))
                if (segmentbreakdown.hasLayover && it.indexOf(segmentbreakdown) != it.size - 1) {
                    linearLayout.addView(createLayoverRow(segmentbreakdown))
                }
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.flight_segment_breakdown, this)
    }

    private fun createSegmentRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(getContext()).inflate(R.layout.flight_segment_row, null)
        val departureArrivalTime = row.findViewById(R.id.departure_arrival_time) as TextView
        val airlineAirplaneType = row.findViewById(R.id.airline_airplane_type) as TextView
        val departureArrivalAirports = row.findViewById(R.id.departure_arrival_airport) as TextView
        val segmentDuration = row.findViewById(R.id.flight_duration) as TextView
        departureArrivalTime.text = PackageFlightUtils.getFlightDepartureArivalTime(context, breakdown.segment.departureTime, breakdown.segment.arrivalTime)
        airlineAirplaneType.text = PackageFlightUtils.getFlightAirlineAirplaneType(context, breakdown.segment)
        departureArrivalAirports.text = PackageFlightUtils.getFlightDepartureArivalCityAirport(context, breakdown.segment)
        segmentDuration.text = PackageFlightUtils.getFlightSegmentDurationString(context, breakdown.segment)
        return row
    }

    private fun createLayoverRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(getContext()).inflate(R.layout.flight_segment_layover_row, null)
        val layoverIn = row.findViewById(R.id.flight_segment_layover_in) as TextView
        val layoverDuration = row.findViewById(R.id.flight_segment_layover_duration) as TextView
        layoverIn.text = Phrase.from(context.resources.getString(R.string.package_flight_overview_layover_in_TEMPLATE))
                    .put("city", breakdown.segment.arrivalCity)
                    .put("airportcode", breakdown.segment.arrivalAirportCode)
                    .format().toString()
        layoverDuration.text = PackageFlightUtils.getFlightSegmentLayoverDurationString(context, breakdown.segment)
        return row
    }
}