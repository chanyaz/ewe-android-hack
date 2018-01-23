package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.rail.RailOutboundHeaderViewModel

class RailOutboundHeaderView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val timesView: TextView by bindView(R.id.outbound_card_time_text)
    val priceView: TextView by bindView(R.id.outbound_card_price_text)
    val operatorTextView: TextView by bindView(R.id.outbound_card_train_operator_text)
    val durationTextView: TextView by bindView(R.id.outbound_card_layover_text)

    private lateinit var viewModel: RailOutboundHeaderViewModel

    init {
        View.inflate(context, R.layout.rail_outbound_header_card, this)
    }

    fun setViewModel(vm: RailOutboundHeaderViewModel) {
        vm.offerPriceObservable.subscribeText(priceView)
        vm.formattedTimeSubject.subscribeText(timesView)
        vm.formattedStopsAndDurationObservable.subscribeText(durationTextView)
        vm.aggregatedOperatingCarrierSubject.subscribeText(operatorTextView)

        vm.contentDescriptionObservable.subscribe { contentDescription ->
            this.contentDescription = contentDescription
        }

        this.viewModel = vm
    }
}
