package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isRichContentShowAmenityEnabled
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.bookings.flights.vm.FlightSegmentBreakdownViewModel
import com.squareup.phrase.Phrase

class FlightSegmentBreakdownView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
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
        vm.updateSeatClassAndCodeSubject.subscribe {
            var childIndex = 0
            it.forEach { seatClassAndBookingCode ->
                val seatClassAndBookingCodeTextView = linearLayout.getChildAt(childIndex).findViewById<TextView>(R.id.flight_seat_class_booking_code)
                val seatClassAndBookingCodeText = FlightServiceClassType.getSeatClassAndBookingCodeText(context, seatClassAndBookingCode.seatClass, seatClassAndBookingCode.bookingCode)
                if (Strings.isNotEmpty(seatClassAndBookingCodeText)) {
                    seatClassAndBookingCodeTextView.visibility = VISIBLE
                    seatClassAndBookingCodeTextView.text = seatClassAndBookingCodeText
                } else {
                    seatClassAndBookingCodeTextView.visibility = GONE
                }
                childIndex += 2
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.flight_segment_breakdown, this)
    }

    private fun createSegmentRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.flight_segment_row, linearLayout, false)
        val departureArrivalTime = row.findViewById<TextView>(R.id.departure_arrival_time)
        val airlineAirplaneType = row.findViewById<TextView>(R.id.airline_airplane_type)
        val departureArrivalAirports = row.findViewById<TextView>(R.id.departure_arrival_airport)
        val operatedBy = row.findViewById<TextView>(R.id.operating_airline_name)
        val seatClassAndBookingCode = row.findViewById<TextView>(R.id.flight_seat_class_booking_code)
        val segmentDuration = row.findViewById<TextView>(R.id.flight_duration)
        departureArrivalTime.text = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(context,
                breakdown.segment.departureDateTimeISO, breakdown.segment.arrivalDateTimeISO, breakdown.segment.elapsedDays)
        airlineAirplaneType.text = FlightV2Utils.getFlightAirlineAndAirplaneType(context, breakdown.segment)
        departureArrivalAirports.text = FlightV2Utils.getFlightDepartureArrivalCityAirport(context, breakdown.segment)
        departureArrivalAirports.contentDescription = FlightV2Utils.getFlightDepartureArrivalCityAirportContDesc(context, breakdown.segment)
        val operatedByString = FlightV2Utils.getOperatingAirlineNameString(context, breakdown.segment)
        if (Strings.isEmpty(operatedByString)) operatedBy.visibility = View.GONE else operatedBy.text = operatedByString
        segmentDuration.text = FlightV2Utils.getFlightSegmentDurationString(context, breakdown.segment)
        segmentDuration.contentDescription = FlightV2Utils.getFlightSegmentDurationContentDescription(context, breakdown.segment)

        val seatClassAndBookingCodeText = FlightServiceClassType.getSeatClassAndBookingCodeText(context, breakdown.segment.seatClass, breakdown.segment.bookingCode)
        if (breakdown.showSeatClassAndBookingCode && Strings.isNotEmpty(seatClassAndBookingCodeText)) {
            seatClassAndBookingCode.visibility = VISIBLE
            seatClassAndBookingCode.text = seatClassAndBookingCodeText
        } else {
            seatClassAndBookingCode.visibility = GONE
        }

        val flightAmenities = breakdown.segment.flightAmenities
        if (isRichContentShowAmenityEnabled() && flightAmenities != null) {
            val richContentWifiView = row.findViewById<ImageView>(R.id.rich_content_wifi)
            richContentWifiView.setVisibility(flightAmenities.wifi)
            val richContentEntertainmentView = row.findViewById<ImageView>(R.id.rich_content_entertainment)
            richContentEntertainmentView.setVisibility(flightAmenities.entertainment)
            val richContentPowerView = row.findViewById<ImageView>(R.id.rich_content_power)
            richContentPowerView.setVisibility(flightAmenities.power)
            val richContentDividerView = row.findViewById<View>(R.id.rich_content_divider)
            richContentDividerView.setVisibility(seatClassAndBookingCode.visibility == VISIBLE &&
                    (flightAmenities.wifi || flightAmenities.entertainment || flightAmenities.power))

            viewmodel.richContentFareFamilyWifiViewStream.subscribeVisibility(richContentWifiView)
            viewmodel.richContentFareFamilyEntertainmentViewStream.subscribeVisibility(richContentEntertainmentView)
            viewmodel.richContentFareFamilyPowerViewStream.subscribeVisibility(richContentPowerView)
            viewmodel.richContentFareFamilyDividerViewStream.subscribeVisibility(richContentDividerView)
        }

        return row
    }

    private fun createLayoverRow(breakdown: FlightSegmentBreakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.flight_segment_layover_row, linearLayout, false)
        val layoverIn = row.findViewById<TextView>(R.id.flight_segment_layover_in)
        val layoverDuration = row.findViewById<TextView>(R.id.flight_segment_layover_duration)
        layoverIn.text = Phrase.from(context.resources.getString(R.string.package_flight_overview_layover_in_TEMPLATE))
                .put("city", breakdown.segment.arrivalCity)
                .put("airportcode", breakdown.segment.arrivalAirportCode)
                .format().toString()
        layoverDuration.text = FlightV2Utils.getFlightSegmentLayoverDurationString(context, breakdown.segment)
        layoverDuration.contentDescription = FlightV2Utils.getFlightSegmentLayoverDurationContentDescription(context, breakdown.segment)
        return row
    }
}
