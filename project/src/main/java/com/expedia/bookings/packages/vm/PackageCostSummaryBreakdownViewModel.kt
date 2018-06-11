package com.expedia.bookings.packages.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageCostSummaryBreakdownModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class PackageCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val packageCostSummaryObservable = PublishSubject.create<PackageCostSummaryBreakdownModel>()

    init {
        packageCostSummaryObservable.subscribe { costSummaryBreakdown ->
            val textSize = context.getResources().getDimension(R.dimen.type_200_text_size)
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()
            breakdowns.add(CostSummaryBreakdownRow.Builder().separator())
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.Hotel))
                    .cost(costSummaryBreakdown.standaloneHotelPrice)
                    .titleTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costTypeface(FontCache.Font.ROBOTO_REGULAR)
                    .titleTextSize(textSize)
                    .costTextSize(textSize)
                    .build())
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.roundtrip_flights))
                    .cost(costSummaryBreakdown.standaloneFlightsPrice)
                    .titleTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costTypeface(FontCache.Font.ROBOTO_REGULAR)
                    .titleTextSize(textSize)
                    .costTextSize(textSize)
                    .build())
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.total))
                    .cost(costSummaryBreakdown.referenceTotalPrice)
                    .titleTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .titleTextSize(textSize)
                    .costTextSize(textSize)
                    .build())
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.savings_for_booking_together))
                    .cost(Phrase.from(context, R.string.discount_minus_amount)
                            .put("amount", costSummaryBreakdown.savings)
                            .format().toString())
                    .titleTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .color(ContextCompat.getColor(context, R.color.cost_summary_breakdown_savings_cost_color))
                    .titleTextSize(textSize)
                    .costTextSize(textSize)
                    .build())
            breakdowns.add(CostSummaryBreakdownRow.Builder().separator())
            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(if (PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN) context.getString(R.string.bundle_total_text) else StrUtils.bundleTotalWithTaxesString(context).toString())
                    .cost(costSummaryBreakdown.referenceTotalPrice)
                    .titleTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costTypeface(FontCache.Font.ROBOTO_REGULAR)
                    .titleColor(ContextCompat.getColor(context, R.color.background_holo_dark))
                    .costColor(ContextCompat.getColor(context, R.color.text_dark))
                    .strikeThrough(true)
                    .build())

            breakdowns.add(CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.includes_flights_hotel))
                    .cost(costSummaryBreakdown.totalPrice)
                    .titleTypeface(FontCache.Font.ROBOTO_REGULAR)
                    .costTypeface(FontCache.Font.ROBOTO_MEDIUM)
                    .costColor(ContextCompat.getColor(context, R.color.background_holo_dark))
                    .titleTextSize(textSize)
                    .build())
            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
            val contDesc = Phrase.from(context, R.string.bundle_overview_price_summary_widget_TEMPLATE)
                    .put("hotel_price", costSummaryBreakdown.standaloneHotelPrice)
                    .put("flights_price", costSummaryBreakdown.standaloneFlightsPrice)
                    .put("reference_price", costSummaryBreakdown.referenceTotalPrice)
                    .put("savings", costSummaryBreakdown.savings)
                    .put("total_price", costSummaryBreakdown.totalPrice)
                    .format().toString()
            priceSummaryContainerDescription.onNext(contDesc)
        }
    }

    override fun trackBreakDownClicked() {
    }
}
