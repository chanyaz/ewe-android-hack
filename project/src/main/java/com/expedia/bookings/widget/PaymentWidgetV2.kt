package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import javax.inject.Inject

public class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val remainingBalance: TextView by bindView(R.id.remaining_balance)
    val totalDueToday: TextView by bindView(R.id.total_due_today)

    val pwpWidget: PayWithPointsWidget by bindView(R.id.pwp_widget)
    var paymentSplitsType = PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
    var isExpediaRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean = false

    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> { vm ->
        vm.totalDueToday.subscribeText(totalDueToday)
        vm.remainingBalanceDueOnCard.subscribeText(remainingBalance)
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
        vm.enableDoneButton.subscribe{ enable ->
            if (paymentOptionsContainer.visibility == View.VISIBLE) {
                viewmodel.enableMenu.onNext(enable && isComplete())
            }
        }
    }
        @Inject set

    lateinit var payWithPointsViewModel: IPayWithPointsViewModel
        @Inject set

override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).hotelComponent().inject(this)
        filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(getCreditCardIcon(), null, null, null)
        Db.setTemporarilySavedCard(null)
    }


    override fun onSelectStoredCard() {
        pwpWidget.refreshPointsForUpdatedBurnAmount()
        super.onSelectStoredCard()
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

    override fun close() {
        if (currentState != PaymentOption::class.java.name && shouldShowPaymentOptions()) {
            show(PaymentOption(), FLAG_CLEAR_TOP)
        } else {
            clearBackStack()
            val activity = context as Activity
            activity.onBackPressed()
        }
    }

    override fun closePopup() {

    }
}
