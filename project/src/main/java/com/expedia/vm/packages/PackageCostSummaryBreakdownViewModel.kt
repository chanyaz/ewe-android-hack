package com.expedia.vm.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class PackageCostSummaryBreakdownViewModel(context: Context) : BaseCostSummaryBreakdownViewModel(context) {
    val packageCostSummaryObservable = PublishSubject.create<PackageCreateTripResponse>()

    init {
        packageCostSummaryObservable.subscribe { createTrip ->
            val packageDetails = createTrip.packageDetails
            val breakdowns = arrayListOf<CostSummaryBreakdownRow>()
            // Hotel + Flights    $330
            breakdowns.add(
                    makeHotelsAndFlightsRow(packageDetails.pricing.basePrice.formattedPrice)
            )

            // 1 room, 6 nights, 2 guests
            breakdowns.add(
                    makeRoomNightsAndGuestRow(packageDetails.hotel.numberOfRooms.toInt(),
                            packageDetails.hotel.numberOfNights.toInt(),
                            packageDetails.hotel.adultCount.toInt()))

            // Taxes and Fees     $50
            breakdowns.add(
                    makeTaxesAndFeesRow(packageDetails.pricing.totalTaxesAndFees.formattedPrice,
                            packageDetails.pricing.taxesAndFeesIncluded))

            // Bundle Discount    -$200
            breakdowns.add(makeTotalSavingRow(packageDetails.pricing.savings.formattedPrice))

            if (packageDetails.pricing.hasResortFee() && PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees()) {
                // Local charges due at hotel
                breakdowns.add(makeDueAtHotelRow(packageDetails.pricing.hotelPricing.mandatoryFees.feeTotal.formattedMoneyFromAmountAndCurrencyCode))
            }

            if (createTrip.selectedCardFees != null) {
                breakdowns.add(makeCardFeeRow(createTrip.selectedCardFees.formattedMoneyFromAmountAndCurrencyCode))
            }

            // -------------------------
            breakdowns.add(CostSummaryBreakdownRow.Builder().separator())

            if (!packageDetails.pricing.hasResortFee() || PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees()) {
                // Bundle Total     $380
                breakdowns.add(makeBundleTotalRow(createTrip.getBundleTotal().formattedPrice))
            }
            if (packageDetails.pricing.hasResortFee()) {
                // Total Due Today  $900
                breakdowns.add(makeTotalDueTodayRow(createTrip.tripTotalPayableIncludingFeeIfZeroPayableByPoints().formattedPrice, PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees()))
            }

            if (packageDetails.pricing.hasResortFee() && !PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees()) {
                // Local charges due at hotel
                breakdowns.add(makeDueAtHotelRow(packageDetails.pricing.hotelPricing.mandatoryFees.feeTotal.formattedMoneyFromAmountAndCurrencyCode))
            }
            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    private fun makeRoomNightsAndGuestRow(roomsCount: Int, nightsCount: Int, guestsCount: Int): CostSummaryBreakdownRow {
        val title = Phrase.from(context, R.string.packages_guest_room_night_TEMPLATE)
                .put("room", StrUtils.formatRoomString(context, roomsCount))
                .put("night", StrUtils.formatNightsString(context, nightsCount))
                .put("guest", StrUtils.formatLowerCaseGuestString(context, guestsCount))
                .format().toString()

        return CostSummaryBreakdownRow.Builder()
                .title(title)
                .build()
    }

    private fun makeDueAtHotelRow(formattedPrice: String): CostSummaryBreakdownRow {
        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.local_charges_due_at_hotel))
                .cost(formattedPrice)
                .build()
    }

    private fun makeCardFeeRow(formattedPrice: String): CostSummaryBreakdownRow {
        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.airline_card_fee))
                .cost(formattedPrice)
                .build()
    }

    private fun makeHotelsAndFlightsRow(formattedPrice: String): CostSummaryBreakdownRow {
        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.cost_summary_breakdown_hotel_flight_summary))
                .cost(formattedPrice)
                .build()
    }

    private fun makeTaxesAndFeesRow(formattedPrice: String, isTaxesAndFeesIncluded: Boolean): CostSummaryBreakdownRow {
        if (isTaxesAndFeesIncluded) {
            val title = Phrase.from(context, R.string.cost_summary_breakdown_taxes_fees_included_TEMPLATE)
                    .put("taxes", formattedPrice)
                    .format().toString()
            return CostSummaryBreakdownRow.Builder()
                    .title(title)
                    .build()
        } else {
            return CostSummaryBreakdownRow.Builder()
                    .title(context.getString(R.string.cost_summary_breakdown_taxes_fees))
                    .cost(formattedPrice)
                    .build()
        }
    }

    private fun makeTotalSavingRow(formattedPrice: String): CostSummaryBreakdownRow {
        val cost = Phrase.from(context, R.string.discount_minus_amount)
                .put("amount", formattedPrice)
                .format().toString()

        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.cost_summary_breakdown_total_savings))
                .cost(cost)
                .color(ContextCompat.getColor(context, R.color.cost_summary_breakdown_savings_cost_color))
                .build()
    }

    private fun makeTotalDueTodayRow(formattedPrice: String, shouldShowBundleTotalWhenResortFees: Boolean): CostSummaryBreakdownRow {
        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.cost_summary_breakdown_total_due_today))
                .cost(formattedPrice)
                .typeface(if (shouldShowBundleTotalWhenResortFees) null else FontCache.Font.ROBOTO_MEDIUM)
                .build()
    }

    private fun makeBundleTotalRow(formattedPrice: String): CostSummaryBreakdownRow {
        return CostSummaryBreakdownRow.Builder()
                .title(context.getString(R.string.bundle_total_text))
                .cost(formattedPrice)
                .typeface(FontCache.Font.ROBOTO_MEDIUM)
                .build()
    }
}