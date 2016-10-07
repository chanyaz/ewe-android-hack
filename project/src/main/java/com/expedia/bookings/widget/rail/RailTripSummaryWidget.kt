package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.rail.RailLegSummaryViewModel
import com.expedia.vm.rail.RailTripSummaryViewModel

class RailTripSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val travelDates: TextView by bindView(R.id.outbound_dates_view)
    val outboundLegSummary: RailLegSummaryWidget by bindView(R.id.rail_outbound_leg_widget)

    private val outboundSummaryViewModel = RailLegSummaryViewModel(context)

    init {
        View.inflate(context, R.layout.rail_overview_summary_widget, this)
        orientation = VERTICAL
        outboundLegSummary.viewModel = outboundSummaryViewModel
    }

    var viewModel: RailTripSummaryViewModel by notNullAndObservable { vm ->
        vm.formattedDatesObservable.subscribeText(travelDates)
        vm.railLegObserver.subscribe(outboundSummaryViewModel.railLegOptionObserver)
        vm.railCardNameObservable.subscribe(outboundSummaryViewModel.railCardAppliedNameSubject)
        vm.fareDescriptionObservable.subscribe(outboundSummaryViewModel.fareDescriptionLabelObservable)
    }
}
