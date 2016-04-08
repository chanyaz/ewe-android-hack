package com.expedia.bookings.widget.packages

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PaymentWidget


class PackagePaymentWidget(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val creditCardCvv: EditText by bindView(R.id.edit_creditcard_cvv)
    val addressLineOne: EditText by bindView(R.id.edit_address_line_one)
    val addressLineTwo: EditText by bindView(R.id.edit_address_line_two)
    val addressCity: EditText by bindView(R.id.edit_address_city)
    val addressState: EditText by bindView(R.id.edit_address_state)
    val editEmailAddress: EditText by bindView(R.id.edit_email_address)

    override fun onFinishInflate() {
        super.onFinishInflate()
        editEmailAddress.onFocusChangeListener = this
        creditCardCvv.onFocusChangeListener = this;
        addressLineOne.onFocusChangeListener = this;
        addressLineTwo.onFocusChangeListener = this;
        addressCity.onFocusChangeListener = this;
        addressState.onFocusChangeListener = this;
    }

    override fun isFilled() : Boolean {
        return super.isFilled()
                || !creditCardCvv.text.toString().isEmpty()
                || !addressLineOne.text.toString().isEmpty()
                || !addressCity.text.toString().isEmpty()
                || !addressState.text.toString().isEmpty()
                || !editEmailAddress.getText().toString().isEmpty();

    }

    override fun isSecureToolbarBucketed() : Boolean {
        return false
    }

    override fun getCreditCardNumberHintResId() : Int {
        return R.string.credit_card_hint
    }

    override fun trackPaymentStoredCCSelect() {
        PackagesTracking().trackCheckoutPaymentSelectStoredCard()
    }

    override fun trackShowPaymentEdit() {
        PackagesTracking().trackCheckoutAddPaymentType()
    }

    override fun trackShowPaymentOptions() {
        PackagesTracking().trackCheckoutSelectPaymentClick()
    }
}
