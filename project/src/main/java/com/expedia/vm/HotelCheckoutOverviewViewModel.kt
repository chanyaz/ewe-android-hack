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
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration

class HotelCheckoutOverviewViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {
    // input
    val newRateObserver = BehaviorSubject.create<HotelCreateTripResponse.HotelProductResponse>()
    // output
    val legalTextInformation = BehaviorSubject.create<SpannableStringBuilder>()
    val disclaimerText = BehaviorSubject.create<Spanned>()
    val depositPolicyText = BehaviorSubject.create<Spanned>()
    val slideToText = BehaviorSubject.create<String>()
    val resetMenuButton = BehaviorSubject.create<Unit>()
    val priceAboveSlider = BehaviorSubject.create<String>()

    init {
        Observable.combineLatest(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse, newRateObserver, { paymentSplitsWithTripTotalPayableAndTripResponse, newRateObserver ->
            object {
                val payingWithPoints = paymentSplitsWithTripTotalPayableAndTripResponse.paymentSplits.payingWithPoints
                val payingWithCards = paymentSplitsWithTripTotalPayableAndTripResponse.paymentSplits.payingWithCards
                val paymentSplitsType = paymentSplitsWithTripTotalPayableAndTripResponse.paymentSplits.paymentSplitsType()
                val isExpediaRewardsRedeemable = paymentSplitsWithTripTotalPayableAndTripResponse.tripResponse.isRewardsRedeemable()
                val dueNowAmount = paymentSplitsWithTripTotalPayableAndTripResponse.tripResponse.getTripTotalExcludingFee()
                val tripTotalPayableInclundingFee  = paymentSplitsWithTripTotalPayableAndTripResponse.tripTotalPayableIncludingFee
                val roomResponse = newRateObserver.hotelRoomResponse
            }
        }).subscribe {
            var tripTotal : String
            if (it.isExpediaRewardsRedeemable) {
                priceAboveSlider.onNext(getPayWithPointsAndOrCardMessaging(it.paymentSplitsType, it.payingWithPoints, it.payingWithCards))
                tripTotal = it.tripTotalPayableInclundingFee.formattedMoneyFromAmountAndCurrencyCode
            } else {
                priceAboveSlider.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template)
                        .put("dueamount", it.dueNowAmount.formattedMoney).format().toString())
                tripTotal = it.roomResponse.rateInfo.chargeableRateInfo.displayTotalPrice.formattedMoney
            }

            disclaimerText.onNext(Html.fromHtml(""))
            depositPolicyText.onNext(Html.fromHtml(""))

            if (it.roomResponse.isPayLater) {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_reserve))
            } else {
                slideToText.onNext(context.getString(R.string.hotelsv2_slide_purchase))
            }

            val currencyCode = it.roomResponse.rateInfo.chargeableRateInfo.currencyCode
            if (it.roomResponse.rateInfo.chargeableRateInfo.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(it.roomResponse.rateInfo.chargeableRateInfo.totalMandatoryFees.toDouble()), currencyCode).formattedMoney
                val text = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, tripTotal));
                disclaimerText.onNext(text)
            } else if (it.roomResponse.isPayLater) {
                if (it.roomResponse.rateInfo.chargeableRateInfo.depositAmount != null) {
                    val depositText = Html.fromHtml(it.roomResponse.depositPolicyAtIndex(0) + " " + it.roomResponse.depositPolicyAtIndex(1))
                    depositPolicyText.onNext(depositText)
                }
                val text = Html.fromHtml(context.getString(R.string.pay_later_disclaimer_TEMPLATE, tripTotal))
                disclaimerText.onNext(text)
            }

            legalTextInformation.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
        }
    }

    fun getPayWithPointsAndOrCardMessaging(paymentSplitsType: PaymentSplitsType, payingWithPoints: PointsAndCurrency, payingWithCards: PointsAndCurrency): String {
        when (paymentSplitsType) {
            PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT ->

            if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
                return Phrase.from(context, R.string.you_are_using_expedia_points_TEMPLATE)
                        .put("amount", payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                        .put("points", NumberFormat.getInstance().format(payingWithPoints.points))
                        .format().toString()
            } else {
                return Phrase.from(context, R.string.you_are_using_bucks_TEMPLATE)
                        .put("amount", payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                        .format().toString()
            }

            PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD ->
                return Phrase.from(context, R.string.your_card_will_be_charged_template)
                        .put("dueamount", payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                        .format().toString()

            PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD ->

                if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
                    return Phrase.from(context, R.string.payment_through_card_and_pwp_points)
                            .put("amount", payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                            .put("points", NumberFormat.getInstance().format(payingWithPoints.points))
                            .put("dueamount", payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                            .format().toString()
                } else {
                    return Phrase.from(context, R.string.payment_through_card_and_bucks)
                            .put("amount", payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                            .put("dueamount", payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode)
                            .format().toString()
                }

        }
    }
}