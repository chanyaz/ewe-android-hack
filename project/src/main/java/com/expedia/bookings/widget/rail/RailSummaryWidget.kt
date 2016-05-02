package com.expedia.bookings.widget.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.rail.RailLegSummaryViewModel

class RailSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val outboundLegSummary: RailLegSummaryWidget by bindView(R.id.rail_outbound_leg_widget)

    init {
        View.inflate(context, R.layout.rail_summary, this)
        orientation = VERTICAL
        outboundLegSummary.viewModel = RailLegSummaryViewModel(context)

    }

}
