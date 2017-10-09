package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.utils.getPaymentType
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.utils.*
import com.expedia.ui.HotelActivity
import com.expedia.util.*
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.mobiata.android.Log
import java.util.*
import javax.inject.Inject

class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {

    val remainingBalance: LinearLayout by bindView(R.id.remaining_balance)
    val remainingBalanceAmount: TextView by bindView(R.id.remaining_balance_amount)
    val totalDueTodayAmount: TextView by bindView(R.id.total_due_today_amount)
    val rewardWidget: ViewStub by bindView(R.id.reward_widget_stub)
    val validCardsList: LinearLayout by bindView(R.id.valid_cards_list)
    val greyCardIcon: ImageView by bindView(R.id.display_credit_card_brand_icon_grey)
    var paymentSplitsType = PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
    var isRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean = false
    val shouldDisplayCardsListOnPaymentForm = isDisplayCardsOnPaymentForm(context)
    val payWithGoogleContainer: RelativeLayout by bindView(R.id.google_payment_container)
    var totalForGooglePay = ""

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
            totalForGooglePay = it.tripTotalPayableIncludingFee.amount.toInt().toString()
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
            creditCardNumber.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    updateCardListOpacity(s.toString())
                }
            })
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutId = ProductFlavorFeatureConfiguration.getInstance().rewardsLayoutId
        if(layoutId != 0){
            rewardWidget.layoutResource = layoutId
            rewardWidget.inflate()
        }
        if (shouldDisplayCardsListOnPaymentForm) {
            updateViewsForDisplayingCardsList()
        }
        isReadyToPay()
        payWithGoogleContainer.setOnClickListener {
            val activity = context as HotelActivity
            val request = createPaymentDataRequest()
            if (request != null) {
                AutoResolveHelper.resolveTask(activity.getPaymentClient().loadPaymentData(request), activity, Constants.LOAD_PAYMENT_DATA_REQUEST_CODE)
            }
        }

        (context as HotelActivity).paymentDataObserver.subscribe { paymentData ->
//            TODO: Parse Payment data, bind card data to sectionBillingInfo, update UI
            doneClicked.onNext(Unit)
//            done clicked closes and brings us back to payment screen
            viewmodel.paymentType.onNext(context.getDrawable(R.drawable.googleg_standard_color_18))
            viewmodel.cardTitle.onNext("${paymentData.cardInfo.cardNetwork} ending in ${paymentData.cardInfo.cardDetails}")
        }

    }

    override fun shouldShowPaymentOptions(): Boolean {
        return isRewardsRedeemable || super.shouldShowPaymentOptions() || payWithGoogleContainer.visibility == View.VISIBLE
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
        creditCardImage.setTag(BookingInfoUtils.getCreditCardIcon(paymentType))
        creditCardImage.setImageResource(BookingInfoUtils.getCreditCardIcon(paymentType))
        val padding = context.resources.getDimensionPixelOffset(R.dimen.default_margin)
        val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, padding, padding, padding)
        creditCardImage.layoutParams = params
        validCardsList.addView(creditCardImage)
    }

    private fun updateViewsForDisplayingCardsList() {
        greyCardIcon.visibility = View.GONE
        validCardsList.visibility = View.VISIBLE
        val paddingLeft = context.resources.getDimensionPixelOffset(R.dimen.small_margin)
        creditCardNumber.setPadding(paddingLeft, creditCardNumber.paddingTop,
                creditCardNumber.paddingRight, creditCardNumber.paddingBottom)
    }

    private fun updateCardListOpacity(cardNumber: String) {
        val paymentType = CurrencyUtils.detectCreditCardBrand(cardNumber, context)
        if (paymentType != null) {
            dimCardsInListThatDontMatchPaymentType(paymentType)
        } else {
            undimAllCards(validCardsList)
        }
    }

    private fun dimCardsInListThatDontMatchPaymentType(paymentType: PaymentType) {
        val validCardRes = BookingInfoUtils.getCreditCardIcon(paymentType)
        for (i in 0..validCardsList.childCount - 1) {
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            if (cardInList.tag != validCardRes) {
                // This is for setting the opacity, where 0% is completely transparent and 100% is completely opaque
                cardInList.setAlpha(AlphaCalculator.getAlphaValue(percentage = 10))
            } else {
                cardInList.setAlpha(AlphaCalculator.getAlphaValue(percentage = 100))
            }
        }
    }

    private fun undimAllCards(validCardsList: LinearLayout) {
        for (i in 0..validCardsList.childCount - 1) {
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            cardInList.setAlpha(AlphaCalculator.getAlphaValue(percentage = 100))
        }
    }


    fun createPaymentDataRequest(): PaymentDataRequest {
        val request = PaymentDataRequest.newBuilder()
                .setTransactionInfo(
                        TransactionInfo.newBuilder()
                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                .setTotalPrice(totalForGooglePay)
                                .setCurrencyCode("USD")
                                .build())
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .setCardRequirements(
                        CardRequirements.newBuilder()
                                .addAllowedCardNetworks(
                                        Arrays.asList(
                                                WalletConstants.CARD_NETWORK_AMEX,
                                                WalletConstants.CARD_NETWORK_DISCOVER,
                                                WalletConstants.CARD_NETWORK_VISA,
                                                WalletConstants.CARD_NETWORK_MASTERCARD))
                                .build())

        val params = PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(
                        WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)
                .addParameter("publicKey", "BI7zNJkGoHq/JTBDyanAWdbZqELJLYd9dkkYEbvIk7G+Umh+KmfPZwUlNAVJL4GQgQmYYQXt4BHDXZ/p+iUkXMc=")
                .build()

        request.setPaymentMethodTokenizationParameters(params)
        return request.build()
    }

    fun isReadyToPay() {
        val request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .build()
        val task = (context as HotelActivity).getPaymentClient().isReadyToPay(request)
        task?.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)
                payWithGoogleContainer.updateVisibility(result)
            } catch (exception: ApiException) {
                Log.d("failed to load paymentData")
            }
        }
    }
}
