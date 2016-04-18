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

class HotelBreakDownViewModel(val context: Context, val hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel) {
    val addRows = BehaviorSubject.create<List<Breakdown>>()
    val dtf = DateTimeFormat.forPattern("M/dd/yyyy")

    init {
        hotelCheckoutSummaryViewModel.newDataObservable.subscribe {
            var breakdowns = arrayListOf<Breakdown>()
            val nightlyRate = Money(BigDecimal(it.nightlyRateTotal.value), it.currencyCode.value)
            breakdowns.add(Breakdown(it.numNights.value, nightlyRate.formattedMoney, false, false))

            var count = 0;
            val checkIn = DateUtils.yyyyMMddToLocalDate(it.checkInDate.value)
            for (rate in it.nightlyRatesPerRoom.value) {
                val date = dtf.print(checkIn.plusDays(count))
                val amount = Money(BigDecimal(rate.rate), it.currencyCode.value)
                val amountStr = if ((amount.isZero))
                    context.getString(R.string.free)
                else
                    amount.formattedMoney

                breakdowns.add(Breakdown(date, amountStr, true, false))
                count++
            }

            // Discount
            val couponRate = it.priceAdjustments.value
            if (couponRate != null && !couponRate.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.discount), couponRate.formattedMoney, false, true))
            }

            // Taxes & Fees
            val surchargeTotal = it.surchargeTotalForEntireStay.value
            val taxStatusType = it.taxStatusType.value
            if (taxStatusType != null && taxStatusType.equals("UNKNOWN")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.unknown), false, false))
            } else if (taxStatusType != null && taxStatusType.equals("INCLUDED")) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), context.getString(R.string.included), false, false))
            } else if (!surchargeTotal.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.taxes_and_fees), surchargeTotal.formattedMoney, false, false))
            }

            // Extra guest fees
            val extraGuestFees = it.extraGuestFees.value
            if (extraGuestFees != null && !extraGuestFees.isZero) {
                breakdowns.add(Breakdown(context.getString(R.string.extra_guest_charge), extraGuestFees.formattedMoney, false, false))
            }

            // Show amount to be paid today in resort or ETP cases
            if (it.isResortCase.value || it.isPayLater.value) {
                var dueTodayText: String
                if (it.isDepositV2.value) {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                } else {
                    dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
                }
                breakdowns.add(Breakdown(dueTodayText, it.dueNowAmount.value, false, false))
            }

            if (it.showFeesPaidAtHotel.value) {
                breakdowns.add(Breakdown(context.getString(R.string.fees_paid_at_hotel), it.feesPaidAtHotel.value, false, false))
            }

            // Total
            breakdowns.add(Breakdown(context.getString(R.string.total_price_label), it.tripTotalPrice.value, false, false))
            addRows.onNext(breakdowns)
        }
    }

    data class Breakdown(val title: String, val cost: String, val isDate: Boolean, val isDiscount: Boolean)
}