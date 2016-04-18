package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

class PackageBreakdownViewModel(val context: Context) {
    val newDataObservable = PublishSubject.create<PackageCreateTripResponse.PackageDetails>()
    val iconVisibilityObservable = PublishSubject.create<Boolean>()

    val addRows = BehaviorSubject.create<List<PackageBreakdown>>()

    init {
        newDataObservable.subscribe { packageDetails ->
            var breakdowns = arrayListOf<PackageBreakdown>()
            var title: String

            if (packageDetails.pricing.taxesAndFeesIncluded) {
                breakdowns.add(PackageBreakdown(context.getString(R.string.package_breakdown_hotel_flight_summary), getFormattedMoney(packageDetails.pricing.packageTotal.amount.toDouble(), packageDetails.pricing.packageTotal.currencyCode), false, false, false, false))
                title = Phrase.from(context, R.string.package_breakdown_taxes_fees_included_TEMPLATE).put("taxes", getFormattedMoney(packageDetails.pricing.totalTaxesAndFees.amount.toDouble(), packageDetails.pricing.totalTaxesAndFees.currencyCode)).format().toString()
                breakdowns.add(PackageBreakdown(title, "", false, false, false, false))
            } else {
                breakdowns.add(PackageBreakdown(context.getString(R.string.package_breakdown_hotel_flight_summary), getFormattedMoney(packageDetails.pricing.basePrice.amount.toDouble(), packageDetails.pricing.basePrice.currencyCode), false, false, false, false))
                title = context.getString(R.string.package_breakdown_taxes_fees)
                breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.totalTaxesAndFees.amount.toDouble(), packageDetails.pricing.totalTaxesAndFees.currencyCode), false, false, false, false))
            }

            // Adding divider line
            breakdowns.add(PackageBreakdown("", "", false, false, false, true))

            title = context.getString(R.string.package_breakdown_total_savings)
            breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.savings.amount.toDouble(), packageDetails.pricing.savings.currencyCode), true, false, false, false))

            title = context.getString(R.string.package_breakdown_total_due_today)
            breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.packageTotal.amount.toDouble(), packageDetails.pricing.packageTotal.currencyCode), false, true, false, false))

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    fun getFormattedMoney(amount: Double, currencyCode: String): String {
        return Money(BigDecimal(amount), currencyCode).formattedMoney
    }

    data class PackageBreakdown(val title: String, val cost: String, val isDiscount: Boolean, val isTotalDue: Boolean, val isTotalCost: Boolean, val isLine: Boolean)
}