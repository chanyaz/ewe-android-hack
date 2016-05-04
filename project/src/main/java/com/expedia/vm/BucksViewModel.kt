package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.utils.withLatestFrom
import com.expedia.vm.interfaces.IBucksViewModel
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class BucksViewModel<T : TripResponse>(paymentModel: PaymentModel<T>, val context: Context) : IBucksViewModel {
    //MESSAGING
    private fun pointsAppliedMessage(paymentSplits: PaymentSplits, tripResponse: TripResponse): String {
        if (paymentSplits.payingWithPoints.points != 0f) {
            return Phrase.from(context, R.string.bucks_applied_TEMPLATE)
                    .put("money", paymentSplits.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        } else {
            return Phrase.from(context, R.string.bucks_available_TEMPLATE)
                    .put("money", tripResponse.getPointDetails()!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        }
    }

    private val programmaticToggle = BehaviorSubject.create<Boolean>(false)

    //Inlet
    override val bucksOpted = BehaviorSubject.create<Boolean>(true)
    override val bucksMessage = paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.filter { it.tripResponse.isRewardsRedeemable() }
            .map { pointsAppliedMessage(it.paymentSplits, it.tripResponse) }

    //Outlet
    override val bucksWidgetVisibility = paymentModel.tripResponses.map { it.isRewardsRedeemable() }
    override val payWithRewardsMessage = bucksOpted.map {
        when (it) {
            true -> context.resources.getString(R.string.paying_with_rewards)
            false -> context.resources.getString(R.string.pay_with_rewards)
        }
    }

    override val pointsAppliedMessageColor = paymentModel.paymentSplits.map {
        when (it.payingWithPoints.points != 0f) {
            true -> ContextCompat.getColor(context, R.color.hotels_primary_color);
            false -> ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color);
        }
    }

    override val updateToggle = paymentModel.paymentSplitsSuggestionUpdates
            .withLatestFrom(paymentModel.swpOpted, paymentModel.tripResponses, bucksOpted, {
                paymentSplitsSuggestionUpdate, isSwpOpted, tripResponse, bucksOpted ->
                object {
                    val isCreateTrip = paymentSplitsSuggestionUpdate.second
                    val isSwpOpted = isSwpOpted
                    val isRewardsRedeemable = tripResponse.isRewardsRedeemable()
                    val bucksOpted = bucksOpted
                }
            })
            .filter { it.isCreateTrip && it.isRewardsRedeemable }
            .doOnNext { if (it.bucksOpted != it.isSwpOpted) programmaticToggle.onNext(true) }
            .map { it.isSwpOpted }

    init {
        bucksOpted.subscribe(paymentModel.togglePaymentByPoints)

        bucksOpted.withLatestFrom(paymentModel.tripResponses, programmaticToggle, {
            bucksOpted, tripResponses, programmaticToggle ->
            object {
                val bucksOpted = bucksOpted
                val tripResponses = tripResponses
                val programmaticToggle = programmaticToggle
            }
        }).doOnNext { programmaticToggle.onNext(false) }.filter { !it.programmaticToggle }.subscribe {
            if (it.bucksOpted) {
                val newPaymentSplits = it.tripResponses.paymentSplitsWhenMaxPayableWithPoints()
                val tripTotal = it.tripResponses.getTripTotalExcludingFee().amount;
                val percentage = NumberUtils.getPercentagePaidWithPointsForOmniture(newPaymentSplits.payingWithPoints.amount.amount, tripTotal)
                HotelV2Tracking().trackPayWithPointsReEnabled(percentage)
            } else {
                HotelV2Tracking().trackPayWithPointsDisabled()
            }
        }
    }
}