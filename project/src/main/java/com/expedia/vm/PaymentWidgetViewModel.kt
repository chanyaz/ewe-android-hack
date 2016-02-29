package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject

class PaymentWidgetViewModel<T : TripResponse>(val context: Context, paymentModel: PaymentModel<T>, payWithPointsViewModel: IPayWithPointsViewModel) : IPaymentWidgetViewModel {
    private fun totalDueTodayMessage(amount: Money) = Phrase.from(context.resources, R.string.pwp_total_due_today_TEMPLATE)
            .put("money", amount.formattedMoneyFromAmountAndCurrencyCode).format().toString()

    private fun remainingBalanceDueOnCardMessage(amount: Money) = Phrase.from(context.resources, R.string.pwp_remaining_balance_due_on_card_TEMPLATE)
            .put("money", amount.formattedMoneyFromAmountAndCurrencyCode).format().toString()

    //Inlets
    override val navigatingOutOfPaymentOptions = PublishSubject.create<Unit>()
    override val hasPwpEditBoxFocus = PublishSubject.create<Boolean>()

    //Outlets
    override val totalDueToday = paymentModel.tripResponses.map { it.getTripTotal().formattedMoneyFromAmountAndCurrencyCode }
    override val remainingBalanceDueOnCard = paymentModel.paymentSplits.map { it.payingWithCards.amount.formattedMoneyFromAmountAndCurrencyCode }
    override val remainingBalanceDueOnCardVisibility = paymentModel.tripResponses.map { it.isExpediaRewardsRedeemable() }
    override val paymentSplitsAndTripResponse = paymentModel.paymentSplitsWithLatestTripResponse
    override val burnAmountApiCallResponsePending = PublishSubject.create<Boolean>()
    override val onStoredCardChosen = PublishSubject.create<Unit>()
    override val enableDoneButton = PublishSubject.create<Boolean>()

    private val isPwpDirty = Observable.combineLatest(hasPwpEditBoxFocus, burnAmountApiCallResponsePending, {
        hasPwpEditBoxFocus, burnAmountApiCallResponsePending ->
        hasPwpEditBoxFocus || burnAmountApiCallResponsePending
    })

    init {
        //Send it off to the Sub ViewModels!
        navigatingOutOfPaymentOptions.subscribe(payWithPointsViewModel.navigatingOutOfPaymentOptions)

        payWithPointsViewModel.hasPwpEditBoxFocus.subscribe(hasPwpEditBoxFocus)
        paymentModel.burnAmountSubject.map { true }.subscribe(burnAmountApiCallResponsePending)

        isPwpDirty.map { !it }.subscribe(enableDoneButton)

        onStoredCardChosen.withLatestFrom(isPwpDirty, { x, y -> y })
                .filter { !it }
                .map { true }
                .subscribe(enableDoneButton)
    }
}
