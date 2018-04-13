package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.packages.vm.AbstractUniversalCKOTotalPriceViewModel
import com.squareup.phrase.Phrase

class FareFamilyTotalPriceViewModel(context: Context) : AbstractUniversalCKOTotalPriceViewModel(context) {

    override fun shouldShowTotalPriceLoadingProgress(): Boolean {
        return false
    }

    override fun getAccessibleContentDescription(isCostBreakdownShown: Boolean, isSlidable: Boolean, isExpanded: Boolean): String {
        return Phrase.from(context, R.string.flight_fare_family_total_price_widget_cont_desc_TEMPLATE)
                .put("farefamily", bundleTextLabelObservable.value)
                .put("totalprice", totalPriceObservable.value)
                .format().toString()
    }
}
