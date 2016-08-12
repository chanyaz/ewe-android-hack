package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.squareup.phrase.Phrase

class FlightSegmentBreakdownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: FlightSegmentBreakdownViewModel by notNullAndObservable { vm ->
        vm.addSegmentRowsObserver.subscribe {
            linearLayout.removeAllViews()
            for (segmentBreakdown in it) {
                linearLayout.addView(createSegmentRow(segmentBreakdown))
                if (segmentBreakdown.hasLayover && it.indexOf(segmentBreakdown) != it.size - 1) {
                    linearLayout.addView(createLayoverRow(segmentBreakdown))
                }
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.flight_segment_breakdown, this)
    }

    private fun createSegmentRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.flight_segment_row, null)
        val departureArrivalTime = row.findViewById(R.id.departure_arrival_time) as TextView
        val airlineAirplaneType = row.findViewById(R.id.airline_airplane_type) as TextView
        val departureArrivalAirports = row.findViewById(R.id.departure_arrival_airport) as TextView
        val operatedBy = row.findViewById(R.id.operating_airline_name) as TextView
        val segmentDuration = row.findViewById(R.id.flight_duration) as TextView
        departureArrivalTime.text = PackageFlightUtils.getFlightDepartureArrivalTimeAndDays(context,
                breakdown.segment.departureDateTimeISO, breakdown.segment.arrivalDateTimeISO, breakdown.segment.elapsedDays)
        airlineAirplaneType.text = PackageFlightUtils.getFlightAirlineAndAirplaneType(context, breakdown.segment)
        departureArrivalAirports.text = PackageFlightUtils.getFlightDepartureArrivalCityAirport(context, breakdown.segment)
        val operatedByString = PackageFlightUtils.getOperatingAirlineNameString(context, breakdown.segment)
        if (Strings.isEmpty(operatedByString)) operatedBy.visibility = View.GONE else operatedBy.text = operatedByString
        segmentDuration.text = PackageFlightUtils.getFlightSegmentDurationString(context, breakdown.segment)
        return row
    }

    private fun createLayoverRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.flight_segment_layover_row, null)
        val layoverIn = row.findViewById(R.id.flight_segment_layover_in) as TextView
        val layoverDuration = row.findViewById(R.id.flight_segment_layover_duration) as TextView
        layoverIn.text = Phrase.from(context.resources.getString(R.string.package_flight_overview_layover_in_TEMPLATE))
                    .put("city", breakdown.segment.arrivalCity)
                    .put("airportcode", breakdown.segment.arrivalAirportCode)
                    .format().toString()
        layoverDuration.text = PackageFlightUtils.getFlightSegmentLayoverDurationString(context, breakdown.segment)
        layoverDuration.contentDescription = PackageFlightUtils.getFlightSegmentLayoverDurationContentDescription(context, breakdown.segment)
        return row
    }
}