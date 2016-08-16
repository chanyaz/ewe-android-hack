package com.expedia.bookings.widget.packages

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.accessibility.AccessibleTextViewForSpinner
import com.expedia.util.subscribeTextChange


class BillingDetailsPaymentWidget(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val creditCardCvv: AccessibleEditText by bindView(R.id.edit_creditcard_cvv)
    val addressLineOne: AccessibleEditText by bindView(R.id.edit_address_line_one)
    val addressLineTwo: AccessibleEditText by bindView(R.id.edit_address_line_two)
    val addressCity: AccessibleEditText by bindView(R.id.edit_address_city)
    val addressState: AccessibleEditText by bindView(R.id.edit_address_state)
    val editEmailAddress: AccessibleEditText by bindView(R.id.edit_email_address)
    val expirationDate: AccessibleTextViewForSpinner by bindView(R.id.edit_creditcard_exp_text_btn)

    override fun onFinishInflate() {
        super.onFinishInflate()
        editEmailAddress.onFocusChangeListener = this
        creditCardCvv.onFocusChangeListener = this;
        addressLineOne.onFocusChangeListener = this;
        addressLineTwo.onFocusChangeListener = this;
        addressCity.onFocusChangeListener = this;
        addressState.onFocusChangeListener = this;
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            compositeSubscription?.add(editEmailAddress.subscribeTextChange(formFilledSubscriber))
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
                || addressState.text.toString().isNotEmpty()
                || editEmailAddress.getText().toString().isNotEmpty()
    }

    override fun isCompletelyFilled(): Boolean {
        return super.isCompletelyFilled()
                && creditCardCvv.text.toString().isNotEmpty()
                && addressLineOne.text.toString().isNotEmpty()
                && addressCity.text.toString().isNotEmpty()
                && addressState.text.toString().isNotEmpty()
                && (editEmailAddress.text.toString().isNotEmpty() || User.isLoggedIn(context))
    }

    override fun isSecureToolbarBucketed(): Boolean {
        return false
    }

    override fun getCreditCardNumberHintResId(): Int {
        return R.string.credit_card_hint
    }

    override fun close() {
        clearBackStack()
        val activity = context as Activity
        activity.onBackPressed()
    }

    override fun closePopup() {
        close()
    }
}
