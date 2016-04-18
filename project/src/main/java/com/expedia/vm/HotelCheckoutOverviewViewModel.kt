package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import java.text.NumberFormat

class HotelCheckoutOverviewViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {
    // input
    val newRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
    // output
    val legalTextInformation = BehaviorSubject.create<SpannableStringBuilder>()
    val disclaimerText = BehaviorSubject.create<Spanned>()
    val depositPolicyText = BehaviorSubject.create<Spanned>()
    val slideToText = BehaviorSubject.create<String>()
    val resetMenuButton = BehaviorSubject.create<Unit>()
    val totalPriceCharged: Observable<String> =
            paymentModel.paymentSplitsWithLatestTripResponse.map {
                object {
                    val payingWithPoints = it.paymentSplits.payingWithPoints
                    val payingWithCards = it.paymentSplits.payingWithCards
                    val paymentSplitsType = it.paymentSplits.paymentSplitsType()
                    val isExpediaRewardsRedeemable = it.tripResponse.isExpediaRewardsRedeemable()
                    val dueNowAmount = it.tripResponse.getTripTotal()
                }
            }.map {
                when (it.isExpediaRewardsRedeemable) {
                    true ->
                        when (it.paymentSplitsType) {
                            PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT ->
                                Phrase.from(context, R.string.you_are_using_expedia_points_TEMPLATE)
                                        .put("amount", it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .put("points", NumberFormat.getInstance().format(it.payingWithPoints.points))
                                        .format().toString()

                            PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD ->
                                Phrase.from(context, R.string.your_card_will_be_charged_template)
                                        .put("dueamount", it.payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .format().toString()

                            PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD ->
                                Phrase.from(context, R.string.payment_through_card_and_pwp_points)
                                        .put("amount", it.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .put("points", NumberFormat.getInstance().format(it.payingWithPoints.points))
                                        .put("dueamount", it.payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                                        .format().toString()
                        }
                    false -> Phrase.from(context, R.string.your_card_will_be_charged_template)
                            .put("dueamount", it.dueNowAmount.formattedMoney).format().toString()
                }
            }

    init {
        newRateObserver.subscribe {
            disclaimerText.onNext(Html.fromHtml(""))
            depositPolicyText.onNext(Html.fromHtml(""))

            val room = it.hotelRoomResponse
            if (room.isPayLater) {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_reserve))
            } else {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_purchase))
            }

            val currencyCode = room.rateInfo.chargeableRateInfo.currencyCode
            val tripTotal = room.rateInfo.chargeableRateInfo.displayTotalPrice.formattedMoney
            if (room.rateInfo.chargeableRateInfo.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(room.rateInfo.chargeableRateInfo.totalMandatoryFees.toDouble()), currencyCode).formattedMoney
                val text = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, tripTotal));
                disclaimerText.onNext(text)
            } else if (room.isPayLater) {
                if (room.rateInfo.chargeableRateInfo.depositAmount != null) {
                    val depositText = Html.fromHtml(room.depositPolicyAtIndex(0) + " " + room.depositPolicyAtIndex(1))
                    depositPolicyText.onNext(depositText)
                }
                val text = Html.fromHtml(context.getString(R.string.pay_later_disclaimer_TEMPLATE, tripTotal))
                disclaimerText.onNext(text)
            }

            legalTextInformation.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
            resetMenuButton.onNext(Unit)
        }
    }
}