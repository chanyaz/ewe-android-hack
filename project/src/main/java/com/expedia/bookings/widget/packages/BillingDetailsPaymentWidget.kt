package com.expedia.bookings.widget.packages

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.extensions.isMaterialFormEnabled
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.MaskedCreditCardEditText
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.rail.widget.CreditCardFeesView
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.util.setInverseVisibility
import com.expedia.util.subscribeMaterialFormsError
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
    val creditCardFeeDisclaimer: TextView by bindView(R.id.card_fee_disclaimer)
    var maskedCreditLayout: TextInputLayout ?= null
    var defaultCreditCardNumberLayout: TextInputLayout ?= null
    var editCountryEditText: EditText ?= null

    val creditCardFeesView = CreditCardFeesView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(creditCardFeesView)
        builder.setTitle(R.string.fees_by_card_type)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    override fun init(paymentViewModel: PaymentViewModel) {
        super.init(paymentViewModel)
        paymentViewModel.onTemporarySavedCreditCardChosen.subscribe { close() }
        paymentViewModel.ccFeeDisclaimer.subscribeTextAndVisibility(creditCardFeeDisclaimer)

        creditCardFeeDisclaimer.setOnClickListener {
            dialog.show()
        }
        paymentViewModel.lineOfBusiness.subscribe { lob ->
            if (lob.isMaterialFormEnabled(context)) {
                setupMaterialForm()
            }
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
            if (getLineOfBusiness().isMaterialFormEnabled(context)) {
                defaultCreditCardNumberLayout?.visibility = GONE
                maskedCreditLayout?.visibility = VISIBLE
            } else {
                creditCardNumber.visibility = VISIBLE
                maskedCreditCard.visibility = GONE
            }

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

    override fun addVisibilitySubscriptions() {
        super.addVisibilitySubscriptions()
        if (!viewmodel.newCheckoutIsEnabled.value) {
            addVisibilitySubscription(creditCardCvv.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressLineOne.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressCity.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(addressState.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(sectionLocation.billingCountryCodeSubject.subscribe(formFilledSubscriber))
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
        if (getLineOfBusiness().isMaterialFormEnabled(context)) {
            maskedCreditLayout?.setInverseVisibility(isCreditCardNumberEmpty)
            defaultCreditCardNumberLayout?.setInverseVisibility(!isCreditCardNumberEmpty)
        }
    }

    @Subscribe fun onAppBackgroundedResumed(@Suppress("UNUSED_PARAMETER") event: Events.AppBackgroundedOnResume) {
        showMaskedCreditCardNumber()
    }

    private fun setupMaterialForm() {
        sectionLocation.removeNonMaterialFields()
        sectionLocation.materialCountryAdapter = CountrySpinnerAdapter(context, CountrySpinnerAdapter.CountryDisplayType.FULL_NAME, R.layout.material_item)
        defaultCreditCardNumberLayout = findViewById(R.id.material_edit_credit_card_number) as TextInputLayout
        editCountryEditText = findViewById(R.id.material_edit_country_button) as EditText
        maskedCreditLayout = findViewById(R.id.material_edit_masked_creditcard_number) as TextInputLayout
        maskedCreditLayout?.visibility = View.GONE

        editCountryEditText?.setOnClickListener{
            showCountryDialog()
        }
        editCountryEditText?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                editCountryEditText?.performClick()
            }
            onFocusChange(view, hasFocus)
        }

        editCountryEditText?.subscribeMaterialFormsError(sectionLocation.billingCountryErrorSubject.map { it }, R.string.error_select_a_billing_country, R.drawable.material_dropdown)

        sectionLocation.billingCountryCodeSubject.subscribe { countryCode ->
            var billingCountry = countryCode
            if (billingCountry.isNullOrBlank()) {
                editCountryEditText?.setText(billingCountry)
                billingCountry = PointOfSale.getPointOfSale().threeLetterCountryCode
                updateCountryDependantFields(billingCountry)
                sectionLocation.updateMaterialPostalFields(PointOfSale.getPointOfSale().pointOfSaleId)
            } else {
                val countryPosition = sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(billingCountry)
                val countryName = sectionLocation.materialCountryAdapter.getItem(countryPosition)
                editCountryEditText?.setText(countryName)
                sectionLocation.billingCountryErrorSubject.onNext(false)
                updateCountryDependantFields(billingCountry)
                sectionLocation.validateCountryDependantFields()
            }
        }
    }

    private fun showCountryDialog() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.resources.getString(R.string.billing_country))
            val adapter = sectionLocation.materialCountryAdapter
            val position = if (sectionLocation.billingCountryCodeSubject.value.isNullOrBlank()) {
                adapter.defaultLocalePosition
            } else {
                adapter.getPositionByCountryThreeLetterCode(sectionLocation.billingCountryCodeSubject.value.toString())
            }

            builder.setSingleChoiceItems(adapter, position) { dialog, position ->
                sectionLocation.billingCountryCodeSubject.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
                dialog.dismiss()
            }

            val alert = builder.create()
            alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
            alert.show()
    }

    private fun updateCountryDependantFields(billingCountry: String) {
        sectionLocation.updateCountryDependantValidation()
        sectionLocation.rebindCountryDependantFields()
        sectionLocation.updateStateFieldBasedOnBillingCountry(billingCountry)
    }
}
