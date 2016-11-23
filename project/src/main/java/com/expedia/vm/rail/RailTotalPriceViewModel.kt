package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.vm.BaseTotalPriceWidgetViewModel

class RailTotalPriceViewModel(context: Context) : BaseTotalPriceWidgetViewModel(false) {
    override fun getAccessibleContentDescription(isCostBreakdownShown: Boolean, isSlidable: Boolean, isExpanded: Boolean): String {
        // TODO copy what makes sense from code for flight/packages
        return ""
    }

    init {
        bundleTextLabelObservable.onNext(context.getString(R.string.total))
        bundleTotalIncludesObservable.onNext(context.getString(R.string.payment_and_ticket_delivery_fees_may_also_apply))
    }

    fun updatePricing(response: RailCreateTripResponse) {
        total.onNext(response.totalPayablePrice)
        costBreakdownEnabledObservable.onNext(true)
    }
}