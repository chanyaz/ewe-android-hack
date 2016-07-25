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
import com.expedia.vm.rail.RailLegSummaryViewModel
import com.expedia.vm.rail.RailSummaryViewModel

class RailSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val outboundLegSummary: RailLegSummaryWidget by bindView(R.id.rail_outbound_leg_widget)
    val travelDates: TextView by bindView(R.id.outbound_dates_view)

    init {
        View.inflate(context, R.layout.rail_summary, this)
        orientation = VERTICAL
        outboundLegSummary.viewModel = RailLegSummaryViewModel(context)

    }

    var viewModel: RailSummaryViewModel by notNullAndObservable { vm ->
        vm.formattedDatesObservable.subscribeText(travelDates)
        vm.selectedOfferObservable.subscribe(outboundLegSummary.viewModel.railOfferObserver)
    }
}
