package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.vm.BaseTotalPriceWidgetViewModel
import com.squareup.phrase.Phrase

class RailTotalPriceViewModel(val context: Context) : BaseTotalPriceWidgetViewModel(false) {
    override fun getAccessibleContentDescription(isCostBreakdownShown: Boolean, isSlidable: Boolean, isExpanded: Boolean): String {
        val costSummaryBuilder = StringBuilder();

        costSummaryBuilder.append(bundleTextLabelObservable.value)
        costSummaryBuilder.append(", ")
        costSummaryBuilder.append(bundleTotalIncludesObservable.value)
        costSummaryBuilder.append(", ")
        costSummaryBuilder.append(totalPriceObservable.value)
        val contentDescription = Phrase.from(context, R.string.rail_cost_breakdown_button_cont_desc_TEMPLATE)
                .put("costsummary", costSummaryBuilder.toString())
                .format().toString()

        return contentDescription
    }

    init {
        bundleTextLabelObservable.onNext(context.getString(R.string.total))
        bundleTotalIncludesObservable.onNext(context.getString(R.string.payment_and_ticket_delivery_fees_may_also_apply))
    }

    fun updatePricing(response: RailCreateTripResponse) {
        total.onNext(response.totalPayablePrice)
        costBreakdownEnabledObservable.onNext(true)
    }

    override fun shouldShowTotalPriceLoadingProgress(): Boolean {
        return false
    }
}