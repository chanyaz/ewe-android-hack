package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.DateUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

class HotelBreakDownViewModel(val context: Context, hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel) {
    val addRows = BehaviorSubject.create<List<Breakdown>>()
    val dtf = DateTimeFormat.forPattern("M/dd/yyyy")

    init {
        hotelCheckoutSummaryViewModel.newDataObservable.subscribe {
            var breakdowns = arrayListOf<Breakdown>()
            val nightlyRate = Money(BigDecimal(it.nightlyRateTotal.value), it.currencyCode.value)
            breakdowns.add(Breakdown(it.numNights.value, nightlyRate.formattedMoney, BreakdownItem.OTHER))

            var count = 0;
            val checkIn = DateUtils.yyyyMMddToLocalDate(it.checkInDate.value)
            for (rate in it.nightlyRatesPerRoom.value) {
                val date = dtf.print(checkIn.plusDays(count))
                val amount = Money(BigDecimal(rate.rate), it.currencyCode.value)
                val amountStr = if ((amount.isZero))
                    context.getString(R.string.free)
                else
                    amount.formattedMoney

                breakdowns.add(Breakdown(date, amountStr, BreakdownItem.DATE))
                count++
            }

            // Taxes & Fees
            val surchargeTotal = it.surchargeTotalForEntireStay.value
            val taxStatusType = it.taxStatusType.value
            if (taxStatusType != null && taxStatusType.equals("UNKNOWN")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.unknown), BreakdownItem.OTHER))
            } else if (!surchargeTotal.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), surchargeTotal.formattedMoney, BreakdownItem.OTHER))
            } else if (taxStatusType != null && taxStatusType.equals("INCLUDED")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.included), BreakdownItem.OTHER))
            }

            // property service fee
            val propertyServiceCharge = it.propertyServiceSurcharge.value
            if (propertyServiceCharge != null) {
                breakdowns.add(Breakdown(context.resources.getString(R.string.property_fee), propertyServiceCharge.formattedMoney, BreakdownItem.OTHER))
            }

            // Extra guest fees
            val extraGuestFees = it.extraGuestFees.value
            if (extraGuestFees != null && !extraGuestFees.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.extra_guest_charge), extraGuestFees.formattedMoney, BreakdownItem.OTHER))
            }

            // Discount
            val couponRate = it.priceAdjustments.value
            if (couponRate != null && !couponRate.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.discount), couponRate.formattedMoney, BreakdownItem.DISCOUNT))
            }

            //When user is paying with Expedia+ points
            if (it.isShoppingWithPoints.value) {
                breakdowns.add(Breakdown(it.burnPointsShownOnHotelCostBreakdown.value,
                        it.burnAmountShownOnHotelCostBreakdown.value, BreakdownItem.DISCOUNT))
            }

            if (it.showFeesPaidAtHotel.value) {
                breakdowns.add(Breakdown(context.getString(R.string.fees_paid_at_hotel), it.feesPaidAtHotel.value, BreakdownItem.OTHER))
            }

            // Total
            breakdowns.add(Breakdown(context.getString(R.string.total_price_label), it.tripTotalPrice.value, BreakdownItem.TRIPTOTAL))

            // Show amount to be paid today in resort or ETP cases
            if (it.isResortCase.value || it.isPayLater.value) {
                var dueTodayText: String
                if (it.isDepositV2.value) {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                } else {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                }
                breakdowns.add(Breakdown(dueTodayText, it.dueNowAmount.value, BreakdownItem.OTHER))
            }
            addRows.onNext(breakdowns)
        }
    }

    data class Breakdown(val title: String, val cost: String, val breakdownItem: BreakdownItem)

    enum class BreakdownItem() {
        DATE,
        DISCOUNT,
        TRIPTOTAL,
        OTHER
    }
}