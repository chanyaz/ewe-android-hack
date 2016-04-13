package com.expedia.bookings.widget.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.User
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PaymentWidget
import com.jakewharton.rxbinding.widget.RxTextView


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

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(editEmailAddress).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(creditCardCvv).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(addressLineOne).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(addressCity).distinctUntilChanged().subscribe(formFilledSubscriber))
            compositeSubscription?.add(RxTextView.afterTextChangeEvents(addressState).distinctUntilChanged().subscribe(formFilledSubscriber))
        }
    }

    override fun isFilled() : Boolean {
        return super.isFilled()
                || !creditCardCvv.text.toString().isEmpty()
                || !addressLineOne.text.toString().isEmpty()
                || !addressCity.text.toString().isEmpty()
                || !addressState.text.toString().isEmpty()
                || !editEmailAddress.getText().toString().isEmpty();
    }

    override fun isCompletelyFilled(): Boolean {
        return super.isCompletelyFilled()
                && creditCardCvv.text.toString().isNotEmpty()
                && addressLineOne.text.toString().isNotEmpty()
                && addressCity.text.toString().isNotEmpty()
                && addressState.text.toString().isNotEmpty()
                && (editEmailAddress.text.toString().isNotEmpty() || User.isLoggedIn(context));
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
