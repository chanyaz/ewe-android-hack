package com.expedia.bookings.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.OnClick
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.User
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.CreditCardUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.WalletUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

public class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val remainingBalance: TextView by bindView(R.id.remaining_balance)
    val totalDueToday: TextView by bindView(R.id.total_due_today)
    val pwpSmallIcon: ImageView by bindView(R.id.pwp_small_icon)
    val filledInCardDetailsMiniView: TextView by bindView(R.id.filled_in_card_details_mini_view)
    val spacerAboveFilledInCardDetailsMiniView: View by bindView(R.id.spacer_above_filled_in_card_details_mini_view)
    val sectionCreditCardContainer: ViewGroup by bindView(R.id.section_credit_card_container)
    val pwpWidget: PayWithPointsWidget by bindView(R.id.pwp_widget)
    val rebindRequested = PublishSubject.create<Unit>()
    var paymentSplitsType: PaymentSplitsType by Delegates.notNull()
    var isExpediaRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean by Delegates.notNull()


    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> { vm ->
        vm.totalDueToday.subscribeText(totalDueToday)
        vm.remainingBalanceDueOnCard.subscribeText(remainingBalance)
        vm.remainingBalanceDueOnCardVisibility.subscribeVisibility(remainingBalance)
        vm.paymentSplitsAndTripResponse.map { it.isCardRequired() }.subscribeEnabled(sectionCreditCardContainer)

        Observable.combineLatest(rebindRequested, vm.paymentSplitsAndTripResponse) { unit, paymentSplitsAndTripResponse -> paymentSplitsAndTripResponse }
                .subscribe {
                    bindPaymentTile(it.tripResponse.isExpediaRewardsRedeemable(), it.paymentSplits.paymentSplitsType())
                    isExpediaRewardsRedeemable = it.tripResponse.isExpediaRewardsRedeemable()
                    paymentSplitsType = it.paymentSplits.paymentSplitsType()
                    isFullPayableWithPoints = !it.isCardRequired()
                    vm.burnAmountApiCallResponsePending.onNext(false)
                }
        vm.enableDoneButton.subscribe{
            enableDoneButton(it)
        }
    }
        @Inject set

    private fun enableDoneButton(enable: Boolean) {
        if (paymentOptionsContainer.visibility == View.VISIBLE) {
            mToolbarListener?.showRightActionButton(enable && isComplete)
        }
    }

    override fun directlyNavigateToPaymentDetails(): Boolean {
        return !isExpediaRewardsRedeemable && !(User.isLoggedIn(context) && !Db.getUser().storedCreditCards.isEmpty()) && Db.getTemporarilySavedCard() == null
                && !(User.isLoggedIn(context) && areFilledInCardDetailsCompleteAndValid())
    }

    lateinit var payWithPointsViewModel: IPayWithPointsViewModel
        @Inject set

    override fun creditCardClicked() {
        presenter.show(CreditCardWidgetExpandedState())
        paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
    }

    @OnClick(R.id.select_payment_button)
    override fun onSelectSavedCardButtonClick() {
        pwpWidget.refreshPointsForUpdatedBurnAmount()
        postDelayed({
            paymentButton.showStoredCards()
        }, 100)
    }

    override fun setPresenter(presenter: Presenter) {
        super.setPresenter(presenter)
        presenter.addTransition(paymentWidgetExpandedToCreditCardWidgetTransition)
    }

    class CreditCardWidgetExpandedState

    private val paymentWidgetExpandedToCreditCardWidgetTransition = object : Presenter.Transition(CheckoutBasePresenter.WidgetExpanded::class.java, CreditCardWidgetExpandedState::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            if (forward) {
                showPaymentDetails()
            } else {
                isExpanded = true
                Ui.hideKeyboard(this@PaymentWidgetV2)
            }
        }
    }

    override fun onMenuButtonPressed() {
        if (billingInfoContainer.visibility == View.VISIBLE) {
            val hasStoredCard = hasStoredCard()
            val billingIsValid = !hasStoredCard && sectionBillingInfo.performValidation()
            val postalIsValid = !hasStoredCard && sectionLocation.performValidation()
            if (hasStoredCard || (billingIsValid && postalIsValid)) {
                if (shouldShowSaveDialog()) {
                    showSaveBillingInfoDialog()
                } else if (directlyNavigateToPaymentDetails()) {
                    isExpanded = false
                } else {
                    mToolbarListener.onWidgetClosed()
                }
            }
        } else if (paymentOptionsContainer.visibility == View.VISIBLE && isComplete) {
            isExpanded = false
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        background = null
        Ui.getApplication(context).hotelComponent().inject(this)
        paymentButton = findViewById(R.id.payment_button_v2) as PaymentButton

        paymentButton.selectPayment.setCompoundDrawablesWithIntrinsicBounds(creditCardIcon, null, ContextCompat.getDrawable(context, R.drawable.ic_picker_down), null)
        paymentButton.setPaymentButtonListener(paymentButtonListener)
        filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(getCreditCardIcon(), null, null, null)
        Db.setTemporarilySavedCard(null)
    }

    override fun showPaymentDetails() {
        if (hasStoredCard()) {
            removeStoredCard();
        }
        super.showPaymentDetails()
        paymentButton.visibility = View.GONE
        if (!hasStoredCard()) {
            paymentButton.selectPayment.setText(R.string.select_saved_cards)
            updatePaymentButtonLeftDrawable(creditCardIcon)
        }
        temporarilySavedCardIsSelected(false)
    }

    override public fun setExpanded(expand: Boolean, animate: Boolean) {
        val wasExpanded = isExpanded
        super.setExpanded(expand, animate)
        if (expand) {
            if (!directlyNavigateToPaymentDetails()) {
                paymentButton.bind()
                paymentButton.visibility = if ((User.isLoggedIn(context) && !Db.getUser().storedCreditCards.isEmpty()) || Db.getTemporarilySavedCard() != null) View.VISIBLE else View.GONE
                paymentWidgetViewModel.hasPwpEditBoxFocus.onNext(false)
                mToolbarListener?.setMenuLabel(getMenuButtonTitle())
            }
            if (!areFilledInCardDetailsCompleteAndValid()) {
                toggleFilledInCardDetailsMiniViewAndItsSpacerVisibility(GONE)
            }
        } else if (wasExpanded) {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
        }
    }

    override fun getPaymentButtonListener(): PaymentButton.IPaymentButtonListener {
        return object : PaymentButton.IPaymentButtonListener {
            override fun onTemporarySavedCreditCardChosen(info: BillingInfo) {
                removeStoredCard()
                selectedCardText(temporarilySavedCardLabel(info.paymentType, info.number), info.paymentType, true)
            }

            override fun onAddNewCreditCardSelected() {
            }

            override fun onStoredCreditCardChosen(card: StoredCreditCard) {
                sectionBillingInfo.billingInfo.storedCard = card
                selectedCardText(card.description, card.type, false)
                paymentWidgetViewModel.onStoredCardChosen.onNext(Unit)
            }

            private fun selectedCardText(selectedCardText: String, paymentType: PaymentType,selects: Boolean) {
                paymentButton.selectPayment.text = selectedCardText
                updatePaymentButtonLeftDrawable(ContextCompat.getDrawable(context, BookingInfoUtils.getTabletCardIcon(paymentType)))
                toggleFilledInCardDetailsMiniViewAndItsSpacerVisibility(GONE)
                temporarilySavedCardIsSelected(selects)
            }
        }
    }

    private fun temporarilySavedCardIsSelected(isSelected: Boolean) {
        val info = Db.getTemporarilySavedCard()
        if (info != null) {
            info.saveCardToExpediaAccount = isSelected
            Db.setTemporarilySavedCard(info)
        }
        bind()
    }

    private fun toggleFilledInCardDetailsMiniViewAndItsSpacerVisibility(visibility: Int){
        filledInCardDetailsMiniView.visibility = visibility
        spacerAboveFilledInCardDetailsMiniView.visibility = visibility
    }

    private fun updatePaymentButtonLeftDrawable(drawableLeft: Drawable) {
        val icons = paymentButton.selectPayment.compoundDrawables
        paymentButton.selectPayment.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, icons[1], icons[2], icons[3])
    }

    override fun bind() {
        rebindRequested.onNext(Unit)
    }

    fun bindPaymentTile(isRedeemable: Boolean, splitsType: PaymentSplitsType) {
        var paymentType: PaymentType? = null
        var paymentInfo = ""
        var paymentOptions = ""

        // Should not perform validation unless the form has information in it
        val isCreditCardSectionFilled = isFilled
        val hasStoredCard = hasStoredCard()
        val isBillingInfoValid = isCreditCardSectionFilled && sectionBillingInfo.performValidation()
        val isPostalCodeValid = isCreditCardSectionFilled && sectionLocation.performValidation()
        val isHotelV2 = lineOfBusiness == LineOfBusiness.HOTELSV2

        if (!WalletUtils.isWalletSupported(lineOfBusiness) && sectionBillingInfo.billingInfo != null
                && sectionBillingInfo.billingInfo.isUsingGoogleWallet) {
            paymentInfo = resources.getString(
                    if (isHotelV2) R.string.checkout_hotelsv2_enter_payment_details_line1 else R.string.enter_payment_details)
            paymentOptions = if (isRedeemable) resources.getString(R.string.checkout_payment_options) else ""
            bindPaymentTileInfo(paymentType, paymentInfo, paymentOptions, splitsType)
            paymentStatusIcon.status = ContactDetailsCompletenessStatus.DEFAULT
            reset()
        }
        //Payment only with points
        else if (isRedeemable && splitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
            bindPaymentTileInfo(PaymentType.POINTS_EXPEDIA_REWARDS
                    , resources.getString(R.string.checkout_paying_with_points_only_line1),
                    resources.getString(R.string.checkout_tap_to_edit), splitsType)

            paymentStatusIcon.status = ContactDetailsCompletenessStatus.COMPLETE
        }
        //Payment with "StoredCard only" OR "StoredCard and points"
        else if (hasStoredCard) {
            val card = sectionBillingInfo.billingInfo.storedCard
            paymentType = card.type
            paymentInfo = card.description
            bindPaymentTileForValidBillingInfo(null, paymentType, isRedeemable, splitsType, paymentInfo)
        }
        //User selects his just filled in card (which he had chosen to save as well with checkout).
        else if (Db.getTemporarilySavedCard() != null && Db.getTemporarilySavedCard().saveCardToExpediaAccount) {
            val info = Db.getTemporarilySavedCard()
            paymentType = info.paymentType
            paymentInfo = temporarilySavedCardLabel(paymentType, info.number)
            bindPaymentTileForValidBillingInfo(info, paymentType, isRedeemable, splitsType, paymentInfo)
        }
        // Card info user entered is valid
        else if (isBillingInfoValid && isPostalCodeValid) {
            val info = sectionBillingInfo.billingInfo
            paymentType = info.paymentType
            paymentInfo = getPaymentInfo(paymentType, info.number)
            bindPaymentTileForValidBillingInfo(info, paymentType, isRedeemable, splitsType, paymentInfo)
        }
        // Card info partially entered & not valid
        else if (isFilled() && (!isBillingInfoValid || !isPostalCodeValid)) {
            paymentInfo = resources.getString(R.string.checkout_hotelsv2_enter_payment_details_line1)
            paymentOptions = resources.getString(
                    if (isRedeemable) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)

            bindPaymentTileInfo(null, paymentInfo, paymentOptions, splitsType)
            paymentStatusIcon.status = ContactDetailsCompletenessStatus.INCOMPLETE
        }
        // Default all fields are empty
        else {
            paymentInfo = resources.getString(R.string.checkout_hotelsv2_enter_payment_details_line1)
            paymentOptions = resources.getString(
                    if (isRedeemable) R.string.checkout_payment_options else R.string.checkout_hotelsv2_enter_payment_details_line2)

            bindPaymentTileInfo(null, paymentInfo, paymentOptions, splitsType)
            paymentStatusIcon.status = ContactDetailsCompletenessStatus.DEFAULT
            reset()
        }
    }

    private fun bindPaymentTileForValidBillingInfo(info: BillingInfo?, paymentType: PaymentType?, isRedeemable: Boolean, splitsType: PaymentSplitsType, cardInfo: String) {
        var paymentInfo = cardInfo
        bindStoredCard(paymentType, paymentInfo)

        val paymentOptions = resources.getString(R.string.checkout_tap_to_edit)

        if (isRedeemable && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
            paymentInfo = Phrase.from(context, R.string.checkout_paying_with_points_and_card_line1)
                    .put("carddescription", paymentInfo)
                    .format().toString()
        }
        bindPaymentTileInfo(paymentType, paymentInfo, paymentOptions, splitsType)
        paymentStatusIcon.status = ContactDetailsCompletenessStatus.COMPLETE

        if (info != null) {
            Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info)
        }
    }

    private fun getPaymentInfo(paymentType: PaymentType?, cardNumber: String): String {
        return Phrase.from(context, R.string.checkout_selected_card)
                .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, paymentType))
                .put("cardno", cardNumber.drop(cardNumber.length - 4))
                .format().toString()
    }

    private fun temporarilySavedCardLabel(paymentType: PaymentType?, cardNumber: String): String {
        return Phrase.from(context, R.string.temporarily_saved_card_TEMPLATE)
                .put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(context, paymentType))
                .put("cardno", cardNumber.drop(cardNumber.length - 4))
                .format().toString()
    }

    private fun bindPaymentTileInfo(paymentType: PaymentType?, paymentInfoText: String, paymentOptionsText: String, splitsType: PaymentSplitsType) {
        bindPaymentTypeName(paymentInfoText)

        bindPaymentOptionText(paymentOptionsText)

        bindPaymentTileIcon(paymentType, splitsType)
    }

    private fun bindPaymentTypeName(paymentInfoText: String) {
        cardInfoName.text = paymentInfoText
    }

    private fun bindPaymentOptionText(paymentOptionText: String) {
        cardInfoExpiration.text = paymentOptionText
        cardInfoExpiration.visibility = View.VISIBLE
    }

    private fun bindPaymentTileIcon(paymentType: PaymentType?, splitsType: PaymentSplitsType) {
        pwpSmallIcon.visibility = if (paymentType != null && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD)
            View.VISIBLE else View.GONE

        cardInfoIcon.setImageDrawable(
                context.resources.getDrawable(
                        if (paymentType != null)
                            BookingInfoUtils.getTabletCardIcon(paymentType)
                        else
                            R.drawable.cars_checkout_cc_default_icon))
    }

    private fun bindStoredCard(paymentType: PaymentType?, cardName: String) {
        storedCardName.text = cardName
        storedCardImageView.setImageDrawable(
                context.resources.getDrawable(
                        if (paymentType != null)
                            BookingInfoUtils.getTabletCardIcon(paymentType)
                        else
                            R.drawable.cars_checkout_cc_default_icon))
    }

    override fun isComplete(): Boolean {
        // Payment through credit card is not required for this car booking.
        if (!isCreditCardRequired) {
            return true
        }
        //If payment is done only through points
        else if (isFullPayableWithPoints) {
            return true
        }
        // If payment through credit card is required check to see if the entered/selected stored CC is valid.
        else if (isCreditCardRequired && hasStoredCard()) {
            return true
        } else if (isCreditCardRequired && isFilled && sectionBillingInfo.performValidation() && sectionLocation.performValidation()) {
            return true
        } else if (isCreditCardRequired && Db.getTemporarilySavedCard() != null && Db.getTemporarilySavedCard().saveCardToExpediaAccount) {
            return true
        }// If payment is required check to see if the entered/selected stored CC is valid.
        return false
    }

    override fun onLogin() {
        payWithPointsViewModel.userSignedIn.onNext(true)
    }

    fun areFilledInCardDetailsCompleteAndValid(): Boolean{
        return (sectionBillingInfo.billingInfo != null && isFilled && sectionBillingInfo.performValidation() && sectionLocation.performValidation())
    }

    override protected fun userChoosesNotToSaveCard() {
        val info = sectionBillingInfo.billingInfo
        toggleFilledInCardDetailsMiniViewAndItsSpacerVisibility(VISIBLE)
        filledInCardDetailsMiniView.text = getPaymentInfo(info.paymentType, info.number)
        Db.setTemporarilySavedCard(null)
        super.userChoosesNotToSaveCard()
    }

    override protected fun userChoosesToSaveCard() {
        super.userChoosesToSaveCard()
        Db.setTemporarilySavedCard(sectionBillingInfo.billingInfo)
        paymentButtonListener.onTemporarySavedCreditCardChosen(Db.getTemporarilySavedCard())
    }
}
