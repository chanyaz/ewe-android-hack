package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class PackageCostSummaryBreakdownViewModel(context: Context): BaseCostSummaryBreakdownViewModel(context) {
    val packageCostSummaryObservable = PublishSubject.create<PackageCreateTripResponse.PackageDetails>()
    init {
        packageCostSummaryObservable.subscribe { packageDetails ->
            val breakdowns = arrayListOf<CostSummaryBreakdown>()
            var title: String

            if (packageDetails.pricing.taxesAndFeesIncluded) {
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(context.getString(R.string.cost_summary_breakdown_hotel_flight_summary)).cost(packageDetails.pricing.packageTotal.formattedPrice).build())

                title = Phrase.from(context, R.string.cost_summary_breakdown_taxes_fees_included_TEMPLATE).put("taxes", packageDetails.pricing.totalTaxesAndFees.formattedPrice).format().toString()
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).build())
            } else {
                breakdowns.add(CostSummaryBreakdown(context.getString(R.string.cost_summary_breakdown_hotel_flight_summary), null, packageDetails.pricing.basePrice.formattedPrice, false, false, false, false))
                title = context.getString(R.string.cost_summary_breakdown_taxes_fees)
                breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(packageDetails.pricing.totalTaxesAndFees.formattedPrice).build())
            }

            // Adding divider line
            breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().isLine(true).build())

            title = context.getString(R.string.cost_summary_breakdown_total_savings)
            breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(packageDetails.pricing.savings.formattedPrice).discount(true).build())

            title = context.getString(R.string.cost_summary_breakdown_total_due_today)
            breakdowns.add(CostSummaryBreakdown.CostSummaryBuilder().title(title).cost(packageDetails.pricing.packageTotal.formattedPrice).totalDue(true).build())

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }

    }

}