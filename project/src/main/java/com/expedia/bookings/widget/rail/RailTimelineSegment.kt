package com.expedia.bookings.widget.rail

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.bindView
import com.mobiata.flightlib.utils.DateTimeUtils

class RailTimelineSegment(context: Context, segment: RailSegment) : FrameLayout(context) {
    val departureTime: TextView by bindView(R.id.rail_timeline_departure_time)
    val arrivalTime: TextView by bindView(R.id.rail_timeline_arrival_time)
    val departureStation: TextView by bindView(R.id.rail_timeline_departure_station)
    val operator: TextView by bindView(R.id.rail_timeline_operator)
    val duration: TextView by bindView(R.id.rail_timeline_duration)
    val arrivalStation: TextView by bindView(R.id.rail_timeline_arrival_station)

    init {
        View.inflate(context, R.layout.widget_rail_details_timeline_segment, this)

        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        departureTime.text = JodaUtils.format(segment.getDepartureDateTime(), dateFormat)
        arrivalTime.text = JodaUtils.format(segment.getArrivalDateTime(), dateFormat)

        departureStation.text = segment.departureStation.stationDisplayName
        arrivalStation.text = segment.arrivalStation.stationDisplayName

        operator.text = segment.operatingCarrier
        duration.text = DateTimeUtils.formatDuration(context.resources, segment.durationMinutes())
    }
}