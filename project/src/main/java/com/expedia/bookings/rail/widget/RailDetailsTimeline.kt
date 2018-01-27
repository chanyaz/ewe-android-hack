package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.util.endlessObserver

class RailDetailsTimeline(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val railLegOptionObserver = endlessObserver<RailLegOption> { railLegOption ->
        removeAllViews()
        addTimelineViews(railLegOption)
    }

    init {
        orientation = VERTICAL
    }

    private fun addTimelineViews(legOption: RailLegOption) {
        if (legOption != null) {
            var previousSegment: RailSegment? = null
            legOption.travelSegmentList?.forEach {
                if (!(previousSegment?.isTransfer ?: false) && !it.isTransfer
                        && it.departureStation.stationCode.equals(previousSegment?.arrivalStation?.stationCode)) {
                    val segmentView = RailTimelineTransfer(context, it.departureStation.stationDisplayName)
                    addView(segmentView)
                }
                if (it.isTransfer) {
                    val transferView = RailTimelineTransfer(context, it)
                    addView(transferView)
                } else {
                    val segmentView = RailTimelineSegment(context, it)
                    addView(segmentView)
                }
                previousSegment = it
            }
        }
    }
}
