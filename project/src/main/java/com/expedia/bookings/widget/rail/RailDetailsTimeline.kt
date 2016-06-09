package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.data.rail.responses.LegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailDetailsViewModel

class RailDetailsTimeline(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    var viewmodel: RailDetailsViewModel by notNullAndObservable { vm ->
        vm.offerViewModel.offerSubject.subscribe {
            removeAllViews()
            addTimelineViews(it)
        }
    }

    private fun addTimelineViews(offer: RailSearchResponse.RailOffer) {
        if (offer.outboundLeg != null) {
            val legOption: LegOption = offer.outboundLeg!!
            var previousSegment: RailSegment? = null
            legOption.travelSegments?.forEach {

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


