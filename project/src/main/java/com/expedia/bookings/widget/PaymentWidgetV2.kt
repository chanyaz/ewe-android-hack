package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import javax.inject.Inject

class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val remainingBalance: LinearLayout by bindView(R.id.remaining_balance)
    val remainingBalanceAmount: TextView by bindView(R.id.remaining_balance_amount)
    val totalDueTodayAmount: TextView by bindView(R.id.total_due_today_amount)

    val pwpWidget: PayWithPointsWidget by bindView(R.id.pwp_widget)
    var paymentSplitsType = PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
    var isExpediaRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean = false

    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> { vm ->
        vm.totalDueToday.subscribeText(totalDueTodayAmount)
        vm.remainingBalanceDueOnCard.subscribeText(remainingBalanceAmount)
        vm.remainingBalanceDueOnCardVisibility.subscribeVisibility(remainingBalance)
        vm.paymentSplitsAndTripResponse.map { it.isCardRequired() }.subscribeEnabled(sectionCreditCardContainer)
        vm.paymentSplitsAndTripResponse.subscribe {
            viewmodel.isRedeemable.onNext(it.tripResponse.isExpediaRewardsRedeemable())
            viewmodel.splitsType.onNext(it.paymentSplits.paymentSplitsType())
            isExpediaRewardsRedeemable = it.tripResponse.isExpediaRewardsRedeemable()
            paymentSplitsType = it.paymentSplits.paymentSplitsType()
            isFullPayableWithPoints = !it.isCardRequired()
            vm.burnAmountApiCallResponsePending.onNext(false)
        }
        enableToolbarMenuButton.subscribe{ enable ->
            if (paymentOptionsContainer.visibility == View.VISIBLE) {
                enableMenuItem.onNext(enable && isComplete())
            }
        }
        vm.isPwpDirty.map { !it }.subscribe(enableToolbarMenuButton)

        viewmodel.onStoredCardChosen.withLatestFrom(vm.isPwpDirty, { x, y -> y })
                .filter { !it }
                .map { true }
                .subscribe(enableToolbarMenuButton)
    }
        @Inject set

    lateinit var payWithPointsViewModel: IPayWithPointsViewModel
        @Inject set

    override fun init(vm: PaymentViewModel) {
        super.init(vm)
        Ui.getApplication(context).hotelComponent().inject(this)
    }

    override fun shouldShowPaymentOptions(): Boolean {
        return isExpediaRewardsRedeemable || super.shouldShowPaymentOptions()
    }

    override fun validateAndBind() {
        if (!isFullPayableWithPoints) {
            super.validateAndBind()
        }
    }

    override fun isComplete(): Boolean {
        return isFullPayableWithPoints || super.isComplete()
    }

    override fun updateToolbarMenu(forward: Boolean) {
        super.updateToolbarMenu(forward)
        if (forward) {
            payWithPointsViewModel.hasPwpEditBoxFocus.onNext(false)
            enableMenuItem.onNext(isComplete())
        } else {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            enableMenuItem.onNext(true)
        }
    }

    override fun back(): Boolean {
        if (currentState == PaymentOption::class.java.name) {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            enableMenuItem.onNext(true)
        }
        return super.back()
    }
}