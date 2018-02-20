package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.rail.data.RailTravelMediumDrawableProvider
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.DateRangeUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class RailTimelineTransfer : FrameLayout {
    val transferText: TextView by bindView(R.id.rail_timeline_transfer_text)
    val travelIcon: ImageView by bindView(R.id.rail_timeline_icon2)

    constructor(context: Context, segment: RailSegment) : super(context) {

        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)
        travelIcon.setImageResource(RailTravelMediumDrawableProvider.findMappedDrawable(segment.travelMedium.travelMediumCode))
        if (segment.isTransfer) {
            transferText.text = Phrase.from(context, R.string.rail_transfer_from_a_to_b_TEMPLATE)
                    .put("formatted_duration", DateRangeUtils.formatDuration(context.resources, segment.durationMinutes()))
                    .put("origin_station_name", segment.departureStation.stationDisplayName)
                    .put("destination_station_name", segment.arrivalStation.stationDisplayName).format().toString()
        } else {
            transferText.text = Phrase.from(context, R.string.rail_travel_from_a_to_b_TEMPLATE)
                    .put("travel_mode", segment.travelMode)
                    .put("origin_station_name", segment.departureStation.stationDisplayName)
                    .put("destination_station_name", segment.arrivalStation.stationDisplayName).format().toString()
        }
    }

    constructor(context: Context, stationName: String) : super(context) {
        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)
        transferText.text = Phrase.from(context, R.string.rail_change_at_station_TEMPLATE)
                .put("station_name", stationName).format().toString()
    }
}
