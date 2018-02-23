package com.expedia.bookings.widget.packages

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.country.CountryConfig
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.subscribeMaterialFormsError
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.vm.PaymentViewModel
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.updatePaddingForOldApi
import com.expedia.bookings.utils.showNewCreditCardExpiryFormField
import com.expedia.bookings.widget.CreditCardExpiryEditText

class MaterialBillingDetailsPaymentWidget(context: Context, attr: AttributeSet) : BillingDetailsPaymentWidget(context, attr) {
    val addressStateLayout by bindView<TextInputLayout>(R.id.material_edit_address_state)
    val maskedCreditLayout by bindView<TextInputLayout>(R.id.material_edit_masked_creditcard_number)
    val defaultCreditCardNumberLayout by bindView<TextInputLayout>(R.id.material_edit_credit_card_number)
    val editCountryEditText by bindView<AccessibleEditTextForSpinner>(R.id.material_edit_country)
    val postalCodeLayout by bindView<TextInputLayout>(R.id.material_edit_address_postal_code)
    val showNewExpiryField = showNewCreditCardExpiryFormField(context)
    val oldCreditExpiryTextLayout by bindView<TextInputLayout>(R.id.material_edit_creditcard_date)
    val newCreditCardExpiryTextLayout by bindView<TextInputLayout>(R.id.material_creditcard_expiry_date)
    val creditCardExpiryText by bindView<CreditCardExpiryEditText>(R.id.edit_creditcard_expiry_date)

    override fun init(vm: PaymentViewModel) {
        super.init(vm)
        setupFields()
        vm.updateBillingCountryFields.subscribe { country ->
            val hideFieldsRequirements = getBillingAddressCountryConfig(country)
            val showStateField = hideFieldsRequirements.stateRequired != CountryConfig.StateRequired.NOT_REQUIRED
            val showPostalField = hideFieldsRequirements.postalCodeRequired
            addressStateLayout.setVisibility(showStateField)
            postalCodeLayout.setVisibility(showPostalField)
            if (!showStateField) addressState.setText("")
            if (!hideFieldsRequirements.postalCodeRequired) creditCardPostalCode.setText("")
            postalCodeLayout.clearFocus()
            addressStateLayout.clearFocus()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val isExtraPaddingRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            editCountryEditText.updatePaddingForOldApi()
        }
        maskedCreditCard.cardNumberTextSubject.subscribe { _ ->
            defaultCreditCardNumberLayout.visibility = VISIBLE
            maskedCreditLayout.visibility = GONE
        }
        oldCreditExpiryTextLayout.setInverseVisibility(showNewExpiryField)
        newCreditCardExpiryTextLayout.setVisibility(showNewExpiryField)
    }

    private fun setupFields() {
        sectionLocation.removeNonMaterialFields()
        editCountryEditText.setOnClickListener {
            showCountryDialog()
        }
        editCountryEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                editCountryEditText.performClick()
            }
            onFocusChange(view, hasFocus)
        }
        editCountryEditText.setSingleLine()
        editCountryEditText.subscribeMaterialFormsError(sectionLocation.billingCountryErrorSubject.map { it }, R.string.error_select_a_billing_country, R.drawable.material_dropdown)

        sectionLocation.billingCountryCodeSubject.subscribe { countryCode ->
            var billingCountry = countryCode
            if (billingCountry.isNullOrBlank()) {
                billingCountry = PointOfSale.getPointOfSale().threeLetterCountryCode
                updateCountryDependantFields(billingCountry)
            } else {
                sectionLocation.billingCountryErrorSubject.onNext(false)
                updateCountryDependantFields(billingCountry)
                sectionLocation.resetCountryDependantValidation()
            }
            val countryPosition = sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(billingCountry)
            val countryName = sectionLocation.materialCountryAdapter.getItem(countryPosition)
            editCountryEditText.setText(countryName)
            sectionLocation.location.countryCode = billingCountry

            val twoLetterCountryCode = sectionLocation.materialCountryAdapter
                    .getItemValue(countryPosition, CountrySpinnerAdapter.CountryDisplayType.TWO_LETTER)
            viewmodel.updateBillingCountryFields.onNext(twoLetterCountryCode)
        }

        sectionLocation.validateBillingCountrySubject.subscribe {
            sectionLocation.billingCountryErrorSubject.onNext(editCountryEditText.text.isNullOrBlank())
        }
        sectionLocation.billingCountryErrorSubject.subscribe { hasError ->
            editCountryEditText.valid = !hasError
        }
    }

    private fun updateCountryDependantFields(billingCountryCode: String) {
        sectionLocation.updateCountryDependantValidation()
        sectionLocation.rebindCountryDependantFields()
        sectionLocation.updateStateFieldBasedOnBillingCountry(billingCountryCode)
    }

    private fun showCountryDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.billing_country))
        val adapter = sectionLocation.materialCountryAdapter
        val countryPosition = if (sectionLocation.billingCountryCodeSubject.value.isNullOrBlank()) {
            adapter.defaultLocalePosition
        } else {
            adapter.getPositionByCountryThreeLetterCode(sectionLocation.billingCountryCodeSubject.value.toString())
        }

        builder.setSingleChoiceItems(adapter, countryPosition) { dialog, position ->
            sectionLocation.billingCountryCodeSubject.onNext(adapter.getItemValue(position, CountrySpinnerAdapter.CountryDisplayType.THREE_LETTER))
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert.show()
    }

    private fun getBillingAddressCountryConfig(country: String): CountryConfig.BillingAddressCountryConfig {
        val billingCountryConfigs = CountryConfig.countryConfig.billingCountryConfigs
        val countryConfigNumber = CountryConfig.getCountryConfigId(country)
        return billingCountryConfigs[countryConfigNumber] as CountryConfig.BillingAddressCountryConfig
    }

    override fun showMaskedCreditCardNumber() {
        super.showMaskedCreditCardNumber()
        val isCreditCardNumberEmpty = creditCardNumber.text.isNullOrEmpty()
        maskedCreditLayout.setInverseVisibility(isCreditCardNumberEmpty)
        defaultCreditCardNumberLayout.setInverseVisibility(!isCreditCardNumberEmpty)
    }
}
