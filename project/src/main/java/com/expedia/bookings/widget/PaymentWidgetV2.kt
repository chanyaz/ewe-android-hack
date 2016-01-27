package com.expedia.bookings.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.User
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.NumberMaskFormatter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.WalletUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeText
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

public class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val remainingBalance: TextView by bindView(R.id.remaining_balance)
    val pwpSmallIcon: ImageView by bindView(R.id.pwp_small_icon)
    val sectionCreditCardContainer: ViewGroup by bindView(R.id.section_credit_card_container)
    val rebindRequested = PublishSubject.create<Unit>()
    var paymentSplitsType: PaymentSplitsType by Delegates.notNull()

    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> {
        it.remainingBalanceDueOnCard.subscribeText(remainingBalance)
        it.paymentSplitsAndTripResponse.map { it.paymentSplits.paymentSplitsType() != PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT }.subscribeEnabled(sectionCreditCardContainer)

        Observable.combineLatest(rebindRequested, it.paymentSplitsAndTripResponse) { unit, paymentSplitsAndTripResponse -> paymentSplitsAndTripResponse }
                .subscribe {
                    bindPaymentTile(it.tripResponse.isExpediaRewardsRedeemable(), it.paymentSplits.paymentSplitsType())
                    paymentSplitsType = it.paymentSplits.paymentSplitsType()
                }
    }
        @Inject set

    override fun onFinishInflate() {
        super.onFinishInflate()
        background = null
        Ui.getApplication(context).hotelComponent().inject(this)
        paymentButton = findViewById(R.id.payment_button_v2) as PaymentButton

        paymentButton.selectPayment.setCompoundDrawablesWithIntrinsicBounds(creditCardIcon, null, ContextCompat.getDrawable(context, R.drawable.ic_picker_down), null)
        paymentButton.setPaymentButtonListener(paymentButtonListener)
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
    }

    override public fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        if (expand) {
            paymentButton.bind()
            paymentButton.visibility = if (User.isLoggedIn(context) && !Db.getUser().storedCreditCards.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun getPaymentButtonListener(): PaymentButton.IPaymentButtonListener {
        return object : PaymentButton.IPaymentButtonListener {
            override fun onAddNewCreditCardSelected() {
            }

            override fun onStoredCreditCardChosen(card: StoredCreditCard) {
                sectionBillingInfo.billingInfo.storedCard = card
                paymentButton.selectPayment.text = card.description
                updatePaymentButtonLeftDrawable(ContextCompat.getDrawable(context, BookingInfoUtils.getTabletCardIcon(card.type)))
                bind()
                Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
                paymentButton.dismissPopup()
            }
        }
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
            paymentInfo = card.description
            paymentType = card.type
            paymentOptions = resources.getString(
                    if (isRedeemable) R.string.checkout_tap_to_edit else R.string.checkout_payment_line2_storedcc)

            bindStoredCard(paymentType, paymentInfo)

            if (isRedeemable && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
                paymentInfo = Phrase.from(context, R.string.checkout_paying_with_points_and_card_line1)
                        .put("carddescription", paymentInfo)
                        .format().toString()
            }

            bindPaymentTileInfo(paymentType, paymentInfo, paymentOptions, splitsType)
            paymentStatusIcon.status = ContactDetailsCompletenessStatus.COMPLETE
        }
        // Card info user entered is valid
        else if (isBillingInfoValid && isPostalCodeValid) {
            val info = sectionBillingInfo.billingInfo
            paymentType = info.paymentType

            paymentInfo = NumberMaskFormatter.obscureCreditCardNumber(info.number)
            bindStoredCard(paymentType, paymentInfo)
            if (isRedeemable && splitsType == PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD) {
                paymentInfo = Phrase.from(context, R.string.checkout_paying_with_points_and_card_line1)
                        .put("carddescription", paymentInfo)
                        .format().toString()
            }

            val expiration = JodaUtils.format(info.expirationDate, "MM/yy")
            paymentOptions = if (isRedeemable)
                resources.getString(R.string.checkout_tap_to_edit)
            else
                resources.getString(R.string.selected_card_template, expiration)

            bindPaymentTileInfo(paymentType, paymentInfo, paymentOptions, splitsType)
            paymentStatusIcon.status = ContactDetailsCompletenessStatus.COMPLETE
            Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info)
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
        else if (paymentSplitsType == PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT) {
            return true
        }
        // If payment through credit card is required check to see if the entered/selected stored CC is valid.
        else if (isCreditCardRequired && hasStoredCard()) {
            return true
        } else if (isCreditCardRequired && isFilled && sectionBillingInfo.performValidation() && sectionLocation.performValidation()) {
            return true
        }// If payment is required check to see if the entered/selected stored CC is valid.
        return false
    }
}
