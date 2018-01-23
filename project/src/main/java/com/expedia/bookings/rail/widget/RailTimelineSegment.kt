package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.rail.data.RailTravelMediumDrawableProvider
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase

class RailTimelineSegment(context: Context, segment: RailSegment) : FrameLayout(context) {
    val departureTime: TextView by bindView(R.id.rail_timeline_departure_time)
    val arrivalTime: TextView by bindView(R.id.rail_timeline_arrival_time)
    val departureStation: TextView by bindView(R.id.rail_timeline_departure_station)
    val operator: TextView by bindView(R.id.rail_timeline_operator)
    val duration: TextView by bindView(R.id.rail_timeline_duration)
    val arrivalStation: TextView by bindView(R.id.rail_timeline_arrival_station)
    val travelIcon: ImageView by bindView(R.id.rail_timeline_icon)
    val journeyDetailsView: LinearLayout by bindView(R.id.journey_details_view)

    init {
        View.inflate(context, R.layout.widget_rail_details_timeline_segment, this)

        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        departureTime.text = JodaUtils.format(segment.getDepartureDateTime(), dateFormat)
        arrivalTime.text = JodaUtils.format(segment.getArrivalDateTime(), dateFormat)

        departureStation.text = segment.departureStation.stationDisplayName
        arrivalStation.text = segment.arrivalStation.stationDisplayName

        operator.text = segment.operatingCarrier
        duration.text = DateTimeUtils.formatDuration(context.resources, segment.durationMinutes())
        travelIcon.setImageResource(RailTravelMediumDrawableProvider.findMappedDrawable(segment.travelMedium.travelMediumCode))
        journeyDetailsView.contentDescription = getContentDescription(segment)
    }

    private fun getContentDescription(segment: RailSegment): String {
        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        return Phrase.from(context, R.string.rail_journey_details_cont_desc_TEMPLATE)
                .put("departurestation", segment.departureStation.stationDisplayName)
                .put("arrivalstation", segment.arrivalStation.stationDisplayName)
                .put("travelmode", segment.travelMode)
                .put("trainoperator", segment.operatingCarrier)
                .put("duration", DateTimeUtils.formatDuration(context.resources, segment.durationMinutes()))
                .put("departuretime", JodaUtils.format(segment.getDepartureDateTime(), dateFormat))
                .put("arrivaltime", JodaUtils.format(segment.getArrivalDateTime(), dateFormat))
                .format()
                .toString()
    }
}
