package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.utils.getPaymentType
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isDisplayCardsOnPaymentForm
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
    val rewardWidget: ViewStub by bindView(R.id.reward_widget_stub)
    val validCardsList: LinearLayout by bindView(R.id.valid_cards_list)
    val greyCardIcon: ImageView by bindView(R.id.display_credit_card_brand_icon_grey)
    val editCreditCardNumber: NumberMaskEditText by bindView(R.id.edit_creditcard_number)
    var paymentSplitsType = PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
    var isRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean = false
    val shouldDisplayCardsListOnPaymentForm = isDisplayCardsOnPaymentForm(context)

    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> { vm ->
        vm.totalDueToday.subscribeText(totalDueTodayAmount)
        vm.remainingBalanceDueOnCard.subscribeText(remainingBalanceAmount)
        vm.remainingBalanceDueOnCardVisibility.subscribeVisibility(remainingBalance)
        vm.paymentSplitsWithTripTotalAndTripResponse.map { it.isCardRequired() }.subscribeEnabled(sectionCreditCardContainer)
        vm.paymentSplitsWithTripTotalAndTripResponse.subscribe {
            viewmodel.isRedeemable.onNext(it.tripResponse.isRewardsRedeemable())
            viewmodel.splitsType.onNext(it.paymentSplits.paymentSplitsType())
            isRewardsRedeemable = it.tripResponse.isRewardsRedeemable()
            paymentSplitsType = it.paymentSplits.paymentSplitsType()
            isFullPayableWithPoints = !it.isCardRequired()
            vm.burnAmountApiCallResponsePending.onNext(false)
            if (shouldDisplayCardsListOnPaymentForm) {
                viewmodel.showValidCards.onNext(it.tripResponse.validFormsOfPayment)
            }
        }

        enableToolbarMenuButton.subscribe{ enable ->
            if (paymentOptionsContainer.visibility == View.VISIBLE) {
                viewmodel.enableMenuItem.onNext(enable && isComplete())
            }
        }

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
        viewmodel.showValidCards.subscribe { validFormsOfPayment ->
            validCardsList.removeAllViewsInLayout()
            for (validType in validFormsOfPayment) {
                addCardInValidCardsList(validType.getPaymentType())
            }
            addCardInValidCardsList(PaymentType.CARD_UNKNOWN)
        }
        if (shouldDisplayCardsListOnPaymentForm) {
            updateViewsForDisplayingCardsList()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutId = ProductFlavorFeatureConfiguration.getInstance().rewardsLayoutId
        if(layoutId != 0){
            rewardWidget.layoutResource = layoutId
            rewardWidget.inflate()
        }
    }

    override fun shouldShowPaymentOptions(): Boolean {
        return isRewardsRedeemable || super.shouldShowPaymentOptions()
    }

    override fun validateAndBind() {
        if (!isFullPayableWithPoints) {
            super.validateAndBind()
        }
    }

    override fun isComplete(): Boolean {
        return isFullPayableWithPoints || super.isComplete()
    }

    override fun updateLegacyToolbarMenu(forward: Boolean) {
        super.updateLegacyToolbarMenu(forward)
        if (forward) {
            payWithPointsViewModel.hasPwpEditBoxFocus.onNext(false)
            viewmodel.enableMenuItem.onNext(isComplete())
        } else {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            viewmodel.enableMenuItem.onNext(true)
        }
    }

    override fun back(): Boolean {
        if (currentState == PaymentOption::class.java.name) {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            viewmodel.enableMenuItem.onNext(true)
        }
        return super.back()
    }

    private fun addCardInValidCardsList(paymentType: PaymentType) {
        val creditCardImage = ImageView(context)
        creditCardImage.setBackgroundResource(BookingInfoUtils.getCreditCardIcon(paymentType))
        val padding = context.resources.getDimensionPixelOffset(R.dimen.default_margin)
        val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, padding, padding, padding)
        creditCardImage.layoutParams = params
        validCardsList.addView(creditCardImage)
    }

    private fun updateViewsForDisplayingCardsList() {
        greyCardIcon.visibility = View.GONE
        validCardsList.visibility = View.VISIBLE
        val editCreditCardNumber = editCreditCardNumber
        val paddingLeft = context.resources.getDimensionPixelOffset(R.dimen.small_margin)
        editCreditCardNumber.setPadding(paddingLeft, editCreditCardNumber.paddingTop,
                editCreditCardNumber.paddingRight, editCreditCardNumber.paddingBottom)
    }
}