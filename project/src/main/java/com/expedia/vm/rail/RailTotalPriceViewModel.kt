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
        bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
    }

    fun updatePricing(response: RailCreateTripResponse) {
        total.onNext(response.totalPrice)
        costBreakdownEnabledObservable.onNext(true)
    }
}