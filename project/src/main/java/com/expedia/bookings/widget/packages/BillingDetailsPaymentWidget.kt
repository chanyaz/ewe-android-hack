package com.expedia.bookings.widget.packages

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.MaskedCreditCardEditText
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.accessibility.AccessibleTextViewForSpinner
import com.expedia.bookings.widget.rail.CreditCardFeesView
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextChange
import com.expedia.vm.PaymentViewModel
import com.squareup.otto.Subscribe

class BillingDetailsPaymentWidget(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val maskedCreditCard: MaskedCreditCardEditText by bindView(R.id.edit_masked_creditcard_number)
    val creditCardCvv: AccessibleEditText by bindView(R.id.edit_creditcard_cvv)
    val addressLineOne: AccessibleEditText by bindView(R.id.edit_address_line_one)
    val addressLineTwo: AccessibleEditText by bindView(R.id.edit_address_line_two)
    val addressCity: AccessibleEditText by bindView(R.id.edit_address_city)
    val addressState: AccessibleEditText by bindView(R.id.edit_address_state)
    val expirationDate: AccessibleTextViewForSpinner by bindView(R.id.edit_creditcard_exp_text_btn)
    val creditCardFeeDisclaimer: TextView by bindView(R.id.card_fee_disclaimer)

    val creditCardFeesView = CreditCardFeesView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(creditCardFeesView)
        builder.setTitle(R.string.fees_by_card_type)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    override fun init(viewModel: PaymentViewModel) {
        super.init(viewModel)
        viewModel.onTemporarySavedCreditCardChosen.subscribe { close() }
        viewModel.ccFeeDisclaimer.subscribeTextAndVisibility(creditCardFeeDisclaimer)

        creditCardFeeDisclaimer.setOnClickListener {
            dialog.show()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
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
            val editTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
            addressCity.setPadding(addressCity.paddingLeft, addressCity.paddingTop, addressCity.paddingRight, editTextSpacing)
            addressState.setPadding(addressState.paddingLeft, addressState.paddingTop, addressState.paddingRight, editTextSpacing)
            creditCardCvv.setPadding(creditCardCvv.paddingLeft, creditCardCvv.paddingTop, creditCardCvv.paddingRight, editTextSpacing)
            creditCardPostalCode.setPadding(creditCardPostalCode.paddingLeft, creditCardPostalCode.paddingTop, creditCardPostalCode.paddingRight, editTextSpacing)
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            compositeSubscription?.add(creditCardCvv.subscribeTextChange(formFilledSubscriber))
            compositeSubscription?.add(addressLineOne.subscribeTextChange(formFilledSubscriber))
            compositeSubscription?.add(addressCity.subscribeTextChange(formFilledSubscriber))
            compositeSubscription?.add(addressState.subscribeTextChange(formFilledSubscriber))
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
        return super.isCompletelyFilled()
                && creditCardCvv.text.toString().isNotEmpty()
                && addressLineOne.text.toString().isNotEmpty()
                && addressCity.text.toString().isNotEmpty()
                && (!isStateRequired() || addressState.text.toString().isNotEmpty())
    }

    override fun isSecureToolbarBucketed(): Boolean {
        return false
    }

    override fun close() {
        clearBackStack()
        val activity = context as Activity
        activity.onBackPressed()
    }

    override fun clearCCAndCVV() {
        super.clearCCAndCVV()
        creditCardCvv.setText("")
    }

    override fun showMaskedCreditCardNumber() {
        val isCreditCardNumberEmpty = creditCardNumber.text.isNullOrEmpty()
        maskedCreditCard.visibility = if (isCreditCardNumberEmpty) GONE else VISIBLE
        creditCardNumber.visibility = if (isCreditCardNumberEmpty) VISIBLE else GONE
        if (!isCreditCardNumberEmpty) maskedCreditCard.showMaskedNumber(creditCardNumber.toFormattedString())
    }

    @Subscribe fun onAppBackgroundedResumed(@Suppress("UNUSED_PARAMETER") event: Events.AppBackgroundedOnResume) {
        showMaskedCreditCardNumber()
    }
}
