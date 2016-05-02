package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.rail.RailLegSummaryViewModel

class RailLegSummaryWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val travelDates: TextView by bindView(R.id.dates_view)
    val travelTimes: TextView by bindView(R.id.times_view)
    val trainOperator: TextView by bindView(R.id.train_operator)
    val duration: TextView by bindView(R.id.layover_view)
    val timeline: RailResultsTimelineWidget by bindView(R.id.timeline_view)
    var outbound = false

    var viewModel: RailLegSummaryViewModel by notNullAndObservable { vm ->
        vm.operatorObservable.subscribeText(trainOperator)
        vm.formattedStopsAndDurationObservable.subscribeText(duration)
        vm.formattedTimesObservable.subscribeText(travelTimes)
        vm.formattedDatesObservable.subscribeText(travelDates)
        vm.legOptionObservable.subscribe {
            timeline.updateLeg(it)
        }
    }

    init {
        View.inflate(getContext(), R.layout.rail_leg_summary, this)
        outbound = true //hardcoding for now until we handle round-trips
    }
}