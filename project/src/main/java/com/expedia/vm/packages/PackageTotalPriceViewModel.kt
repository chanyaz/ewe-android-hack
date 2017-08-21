package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase

class PackageTotalPriceViewModel(context: Context, isSlidable: Boolean = false) : AbstractUniversalCKOTotalPriceViewModel(context, isSlidable) {
    override fun getAccessibleContentDescription(isCostBreakdownShown: Boolean,
                                                 isSlidable: Boolean, isExpanded: Boolean): String {
        val description = if (isCostBreakdownShown || (costBreakdownEnabledObservable.value != null && costBreakdownEnabledObservable.value)) {
            Phrase.from(context, R.string.bundle_total_price_widget_cost_breakdown_cont_desc_TEMPLATE)
                    .put("totalprice", totalPriceObservable.value)
                    .put("savings", savingsPriceObservable.value)
                    .format().toString()
        } else if (!isSlidable && savingsPriceObservable.value != null && totalPriceObservable.value != null) {
            Phrase.from(context, R.string.bundle_overview_price_widget_TEMPLATE)
                    .put("totalprice", totalPriceObservable.value)
                    .put("savings", savingsPriceObservable.value)
                    .format().toString()
        } else if (isExpanded) {
            val params = Db.getPackageParams()
            Phrase.from(context, R.string.trip_overview_price_widget_expanded_TEMPLATE)
                    .put("city_name", StrUtils.formatCity(params.destination))
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString()
        } else if (pricePerPersonObservable.value != null) {
            Phrase.from(context, R.string.bundle_overview_price_widget_button_open_TEMPLATE)
                    .put("price_per_person", pricePerPersonObservable.value)
                    .format().toString()
        } else {
            ""
        }
        return description
    }

    override fun shouldShowTotalPriceLoadingProgress(): Boolean {
        return false
    }
}