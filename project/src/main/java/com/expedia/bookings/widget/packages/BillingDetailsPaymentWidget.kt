package com.expedia.bookings.widget.packages

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextChange
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.MaskedCreditCardEditText
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.rail.widget.CreditCardFeesView
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.extensions.updatePaddingForOldApi
import com.expedia.bookings.extensions.setVisibility
import com.expedia.vm.PaymentViewModel

open class BillingDetailsPaymentWidget(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val maskedCreditCard: MaskedCreditCardEditText by bindView(R.id.edit_masked_creditcard_number)
    val creditCardCvv: AccessibleEditText by bindView(R.id.edit_creditcard_cvv)
    val addressLineOne: AccessibleEditText by bindView(R.id.edit_address_line_one)
    val addressLineTwo: AccessibleEditText by bindView(R.id.edit_address_line_two)
    val addressCity: AccessibleEditText by bindView(R.id.edit_address_city)
    val addressState: AccessibleEditText by bindView(R.id.edit_address_state)
    val creditCardFeeDisclaimer: TextView by bindView(R.id.card_fee_disclaimer)
    val cardInfoSummary: LinearLayout by bindView(R.id.card_info_summary)

    val creditCardFeesView = CreditCardFeesView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(creditCardFeesView)
        builder.setTitle(R.string.fees_by_card_type)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, _ -> dialog.dismiss() })
        builder.create()
    }

    override fun init(vm: PaymentViewModel) {
        super.init(vm)
        vm.ccFeeDisclaimer.subscribeTextAndVisibility(creditCardFeeDisclaimer)

        creditCardFeeDisclaimer.setOnClickListener {
            dialog.show()
        }
        vm.removeBillingAddressForApac.subscribe { shouldHide ->
            billingAddressTitle.setVisibility(!shouldHide)
            sectionLocation.setVisibility(!shouldHide)
            if (shouldHide) viewmodel.createFakeAddressObservable.onNext(Unit)
        }
        vm.populateFakeBillingAddress.subscribe { location ->
            sectionLocation.bind(location)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        sectionLocation.removeNonMaterialFields()
        creditCardCvv.onFocusChangeListener = this
        addressLineOne.onFocusChangeListener = this
        addressLineTwo.onFocusChangeListener = this
        addressCity.onFocusChangeListener = this
        addressState.onFocusChangeListener = this
        maskedCreditCard.cardNumberTextSubject.subscribe { text ->
            creditCardNumber.setText(text)
            creditCardNumber.setSelection(text.length)
            creditCardNumber.visibility = VISIBLE
            maskedCreditCard.visibility = GONE
        }
        val isExtraPaddingRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            addressCity.updatePaddingForOldApi()
            addressState.updatePaddingForOldApi()
            creditCardCvv.updatePaddingForOldApi()
            creditCardPostalCode.updatePaddingForOldApi()
            expirationDate.updatePaddingForOldApi()
            addressLineOne.updatePaddingForOldApi()
            addressLineTwo.updatePaddingForOldApi()
        }
    }

    override fun addVisibilitySubscriptions() {
        super.addVisibilitySubscriptions()
        if (!viewmodel.newCheckoutIsEnabled.value) {
            addVisibilitySubscription(creditCardCvv.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressLineOne.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressCity.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressState.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(sectionLocation.billingCountryCodeSubject.subscribeObserver(formFilledSubscriber))
            sectionLocation.billingCountryCodeSubject.subscribe { sectionLocation.resetValidation(R.id.edit_address_state, true) }
        }
    }

    override fun isAtLeastPartiallyFilled(): Boolean {
        return super.isAtLeastPartiallyFilled()
                || creditCardCvv.text.toString().isNotEmpty()
                || addressLineOne.text.toString().isNotEmpty()
                || addressCity.text.toString().isNotEmpty()
                || (isStateRequired() && addressState.text.toString().isNotEmpty())
    }

    override fun isCompletelyFilled(): Boolean {
        return (super.isCompletelyFilled()
                && creditCardCvv.text.toString().isNotEmpty()
                && addressLineOne.text.toString().isNotEmpty()
                && addressCity.text.toString().isNotEmpty()
                && (!isStateRequired() || addressState.text.toString().isNotEmpty())) || hasStoredCard()
    }

    override fun close() {
        clearBackStack()
        val activity = context as Activity
        activity.onBackPressed()
    }

    override fun clearCVV() {
        super.clearCVV()
        creditCardCvv.setText("")
    }

    override fun showMaskedCreditCardNumber() {
        val isCreditCardNumberEmpty = creditCardNumber.text.isNullOrEmpty()
        maskedCreditCard.visibility = if (isCreditCardNumberEmpty) GONE else VISIBLE
        creditCardNumber.visibility = if (isCreditCardNumberEmpty) VISIBLE else GONE
        if (!isCreditCardNumberEmpty) maskedCreditCard.showMaskedNumber(creditCardNumber.toFormattedString())
    }
}
