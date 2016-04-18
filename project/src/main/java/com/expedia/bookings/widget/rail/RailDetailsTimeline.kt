package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailDetailsViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase

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
            val legOption: RailSearchResponse.LegOption = offer.outboundLeg!!
            var previousSegment: RailSearchResponse.RailSegment? = null
            legOption.segmentList?.forEach {

                if (!(previousSegment?.isTransfer ?: false) && !it.isTransfer
                        && it.departureStationDetails.stationCode.equals(previousSegment?.arrivalStationDetails?.stationCode)) {

                    val segmentView = RailTimelineTransfer(context, it.departureStationDetails.stationName)
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

class RailTimelineSegment(context: Context, segment: RailSearchResponse.RailSegment) : FrameLayout(context) {
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

        departureStation.text = segment.departureStationDetails.stationName
        arrivalStation.text = segment.arrivalStationDetails.stationName

        operator.text = segment.supplier.operatingCarrier
        duration.text = DateTimeUtils.formatDuration(context.resources, segment.durationInMinutes)
    }
}

class RailTimelineTransfer : FrameLayout {
    val transferText: TextView by bindView(R.id.rail_timeline_transfer_text)

    constructor(context: Context, segment: RailSearchResponse.RailSegment) : super(context) {
        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)

        if ("Transfer".equals(segment.travelMode)) {
            transferText.text = Phrase.from(context, R.string.rail_walk_from_a_to_b_TEMPLATE)
                    .put("origin", segment.departureStationDetails.stationName)
                    .put("destination", segment.arrivalStationDetails.stationName).format().toString()
        } else {
            transferText.text = Phrase.from(context, R.string.rail_travel_from_a_to_b_TEMPLATE)
                    .put("travelmode", segment.travelMode)
                    .put("origin", segment.departureStationDetails.stationName)
                    .put("destination", segment.arrivalStationDetails.stationName).format().toString()
        }
    }

    constructor(context: Context, stationName: String) : super(context) {
        View.inflate(context, R.layout.widget_rail_details_timeline_transfer, this)
        transferText.text = Phrase.from(context, R.string.rail_change_at_station_TEMPLATE)
                .put("stationname", stationName).format().toString()
    }
}
