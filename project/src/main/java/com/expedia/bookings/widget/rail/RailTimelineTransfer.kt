package com.expedia.bookings.widget.rail

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase

class RailTimelineTransfer : FrameLayout {
    val transferText: TextView by bindView(R.id.rail_timeline_transfer_text)
    val travelIcon: ImageView by bindView(R.id.rail_timeline_icon2)

    constructor(context: Context, segment: RailSegment) : super(context) {

        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)
        travelIcon.setImageResource(RailTransferMode.findMappedDrawable(segment.travelMode))
        if (segment.isTransfer) {
            transferText.text = Phrase.from(context, R.string.rail_walk_from_a_to_b_TEMPLATE)
                    .put("origin", segment.departureStation.stationDisplayName)
                    .put("destination", segment.arrivalStation.stationDisplayName).format().toString()
        } else {
            transferText.text = Phrase.from(context, R.string.rail_travel_from_a_to_b_TEMPLATE)
                    .put("travelmode", segment.travelMode)
                    .put("origin", segment.departureStation.stationDisplayName)
                    .put("destination", segment.arrivalStation.stationDisplayName).format().toString()
        }
    }

    constructor(context: Context, stationName: String) : super(context) {
        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)
        transferText.text = Phrase.from(context, R.string.rail_change_at_station_TEMPLATE)
                .put("stationname", stationName).format().toString()
    }
}
