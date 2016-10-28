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
    val outboundDateView: TextView by bindView(R.id.outbound_dates_view)
    val outboundLegSummary: RailLegSummaryWidget by bindView(R.id.rail_outbound_leg_widget)

    val inboundDateView: TextView by bindView(R.id.inbound_dates_view)
    val inboundLegSummary: RailLegSummaryWidget by bindView(R.id.rail_inbound_leg_widget)

    private val outboundSummaryViewModel = RailLegSummaryViewModel(context)
    private val inboundSummaryViewModel = RailLegSummaryViewModel(context)

    init {
        View.inflate(context, R.layout.rail_overview_summary_widget, this)
        orientation = VERTICAL
        outboundLegSummary.bindViewModel(outboundSummaryViewModel)
        inboundLegSummary.bindViewModel(inboundSummaryViewModel)
    }

    var viewModel: RailTripSummaryViewModel by notNullAndObservable { vm ->
        vm.railOfferObserver.subscribe { offer ->
            outboundSummaryViewModel.railProductObserver.onNext(offer.railProductList[0])
            if (offer.isRoundTrip) {
                inboundSummaryViewModel.railProductObserver.onNext(offer.railProductList[1])
                inboundLegSummary.visibility = View.VISIBLE
            }
        }

        vm.formattedOutboundDateObservable.subscribeText(outboundDateView)
        vm.formattedInboundDateObservable.subscribeTextAndVisibility(inboundDateView)

        vm.railOutboundLegObserver.subscribe(outboundSummaryViewModel.railLegOptionObserver)
        vm.railInboundLegObserver.subscribe(inboundSummaryViewModel.railLegOptionObserver)

        outboundSummaryViewModel.showLegInfoObservable.subscribe(vm.moreInfoOutboundClicked)
        inboundSummaryViewModel.showLegInfoObservable.subscribe(vm.moreInfoInboundClicked)
    }

    fun reset() {
        inboundDateView.visibility = View.GONE
        inboundLegSummary.visibility = View.GONE
        outboundLegSummary.reset()
        inboundLegSummary.reset()
    }
}
