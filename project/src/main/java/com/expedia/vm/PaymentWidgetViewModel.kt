package com.expedia.vm

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

public class PaymentWidgetViewModel<T : TripResponse>(paymentModel: PaymentModel<T>, val resources: Resources) : IPaymentWidgetViewModel {
    private fun totalDueTodayMessage(amount: Money) = Phrase.from(resources, R.string.pwp_total_due_today_TEMPLATE)
            .put("money", amount.formattedMoneyFromAmountAndCurrencyCode).format().toString()

    private fun remainingBalanceDueOnCardMessage(amount: Money) = Phrase.from(resources, R.string.pwp_remaining_balance_due_on_card_TEMPLATE)
            .put("money", amount.formattedMoneyFromAmountAndCurrencyCode).format().toString()

    override val totalDueToday = paymentModel.tripResponses.map { totalDueTodayMessage(it.getTripTotal()) }
    override val remainingBalanceDueOnCard = paymentModel.paymentSplits.map { remainingBalanceDueOnCardMessage(it.payingWithCards.amount) }
    override val remainingBalanceDueOnCardVisibility = paymentModel.tripResponses.map { it.isExpediaRewardsRedeemable() }
    override val paymentSplitsAndTripResponse = paymentModel.paymentSplitsAndLatestTripResponse
    override val discardPendingCurrencyToPointsAPISubscription = PublishSubject.create<Unit>()

    init {
        //Send it off to the Model!
        discardPendingCurrencyToPointsAPISubscription.subscribe(paymentModel.discardPendingCurrencyToPointsAPISubscription)
    }
}
