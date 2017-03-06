package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.vm.BaseTotalPriceWidgetViewModel
import com.squareup.phrase.Phrase

class BundleTotalPriceViewModel(val context: Context,
                                val isSlidable: Boolean = false) : BaseTotalPriceWidgetViewModel(isSlidable) {
    init {
        pricePerPerson.subscribe {
            perPersonTextLabelObservable.onNext(true)
            pricePerPersonObservable.onNext(it.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
            val description = Phrase.from(context, R.string.bundle_overview_price_widget_button_open_TEMPLATE)
                    .put("price_per_person", pricePerPersonObservable.value)
                    .format().toString()
            contentDescriptionObservable.onNext(description)
        }

        savings.subscribe { savings ->
            if (!savings.isZero && !savings.isLessThanZero) {
                val packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                        .put("savings", savings.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                        .format().toString()
                savingsPriceObservable.onNext(packageSavings)
                contentDescriptionObservable.onNext(getAccessibleContentDescription(isSlidable))
            } else savingsPriceObservable.onNext("")
        }

        costBreakdownEnabledObservable.subscribe { isCostBreakdownEnabled ->
            contentDescriptionObservable.onNext(getAccessibleContentDescription(isCostBreakdownEnabled))
        }
    }

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
            Phrase.from(context, R.string.bundle_overview_price_widget_expanded_TEMPLATE)
                    .put("city_name", StrUtils.formatCity(params.destination))
                    .put("startdate", DateUtils.localDateToMMMd(params.startDate))
                    .put("enddate", DateUtils.localDateToMMMd(params.endDate))
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
}