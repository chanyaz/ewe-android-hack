package com.expedia.bookings.widget.packages

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.country.CountryConfig
import com.expedia.bookings.data.extensions.isMaterialFormEnabled
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.subscribeMaterialFormsError
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextChange
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.rail.widget.CreditCardFeesView
import com.expedia.bookings.section.CountrySpinnerAdapter
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.showNewCreditCardExpiryFormField
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.bookings.extensions.updatePaddingForOldApi
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.widget.CreditCardExpiryEditText
import com.expedia.bookings.widget.MaskedCreditCardEditText
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.TextView
import com.expedia.vm.PaymentViewModel
import com.squareup.otto.Subscribe

class BillingDetailsPaymentWidget(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val maskedCreditCard: MaskedCreditCardEditText by bindView(R.id.edit_masked_creditcard_number)
    val creditCardCvv: AccessibleEditText by bindView(R.id.edit_creditcard_cvv)
    val addressLineOne: AccessibleEditText by bindView(R.id.edit_address_line_one)
    val addressLineTwo: AccessibleEditText by bindView(R.id.edit_address_line_two)
    val addressCity: AccessibleEditText by bindView(R.id.edit_address_city)
    var addressStateLayout: TextInputLayout ? = null
    val addressState: AccessibleEditText by bindView(R.id.edit_address_state)
    val creditCardFeeDisclaimer: TextView by bindView(R.id.card_fee_disclaimer)
    var maskedCreditLayout: TextInputLayout ? = null
    var defaultCreditCardNumberLayout: TextInputLayout ? = null
    var editCountryEditText: AccessibleEditTextForSpinner ? = null
    var postalCodeLayout: TextInputLayout ? = null
    val cardInfoSummary: LinearLayout by bindView(R.id.card_info_summary)
    var creditCardExpiryText: CreditCardExpiryEditText ? = null
    val showNewExpiryField = showNewCreditCardExpiryFormField(context)
    var oldCreditExpiryTextLayout: TextInputLayout ? = null
    var newCreditCardExpiryTextLayout: TextInputLayout? = null
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
        vm.lineOfBusiness.subscribe { lob ->
            if (lob.isMaterialFormEnabled(context)) {
                setupMaterialForm()
            }
        }
        vm.updateBillingCountryFields.subscribe { country ->
            val hideFieldsRequirements = getBillingAddressCountryConfig(country)
            val showStateField = hideFieldsRequirements.stateRequired != CountryConfig.StateRequired.NOT_REQUIRED
            val showPostalField = hideFieldsRequirements.postalCodeRequired
            addressStateLayout?.setVisibility(showStateField)
            postalCodeLayout?.setVisibility(showPostalField)
            if (!showStateField) addressState.setText("")
            if (!hideFieldsRequirements.postalCodeRequired) creditCardPostalCode.setText("")
            postalCodeLayout?.clearFocus()
            addressStateLayout?.clearFocus()
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
        creditCardCvv.onFocusChangeListener = this
        addressLineOne.onFocusChangeListener = this
        addressLineTwo.onFocusChangeListener = this
        addressCity.onFocusChangeListener = this
        addressState.onFocusChangeListener = this
        maskedCreditCard.cardNumberTextSubject.subscribe { text ->
            if (getLineOfBusiness().isMaterialFormEnabled(context)) {
                defaultCreditCardNumberLayout?.visibility = VISIBLE
                maskedCreditLayout?.visibility = GONE
            }
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
            editCountryEditText?.updatePaddingForOldApi()
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

        defaultCreditCardNumberLayout = findViewById<TextInputLayout>(R.id.material_edit_credit_card_number)
        editCountryEditText = findViewById<AccessibleEditTextForSpinner>(R.id.material_edit_country)
        maskedCreditLayout = findViewById<TextInputLayout>(R.id.material_edit_masked_creditcard_number)
        addressStateLayout = findViewById<TextInputLayout>(R.id.material_edit_address_state)
        postalCodeLayout = findViewById<TextInputLayout>(R.id.material_edit_address_postal_code)
        creditCardExpiryText = findViewById<CreditCardExpiryEditText>(R.id.edit_creditcard_expiry_date)
        oldCreditExpiryTextLayout = findViewById<TextInputLayout>(R.id.material_edit_creditcard_date)
        newCreditCardExpiryTextLayout = findViewById<TextInputLayout>(R.id.material_creditcard_expiry_date)

        oldCreditExpiryTextLayout?.setInverseVisibility(showNewExpiryField)
        newCreditCardExpiryTextLayout?.setVisibility(showNewExpiryField)

        editCountryEditText?.setOnClickListener {
            showCountryDialog()
        }
        editCountryEditText?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Ui.hideKeyboard(this)
                editCountryEditText?.performClick()
            }
            onFocusChange(view, hasFocus)
        }
        editCountryEditText?.setSingleLine()
        editCountryEditText?.subscribeMaterialFormsError(sectionLocation.billingCountryErrorSubject.map { it }, R.string.error_select_a_billing_country, R.drawable.material_dropdown)

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
            editCountryEditText?.setText(countryName)
            sectionLocation.location.countryCode = billingCountry

            val twoLetterCountryCode = sectionLocation.materialCountryAdapter
                    .getItemValue(countryPosition, CountrySpinnerAdapter.CountryDisplayType.TWO_LETTER)
            viewmodel.updateBillingCountryFields.onNext(twoLetterCountryCode)
        }

        sectionLocation.validateBillingCountrySubject.subscribe {
            sectionLocation.billingCountryErrorSubject.onNext(editCountryEditText?.text.isNullOrBlank())
        }
        sectionLocation.billingCountryErrorSubject.subscribe { hasError ->
            editCountryEditText?.valid = !hasError
        }
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

    private fun updateCountryDependantFields(billingCountryCode: String) {
        sectionLocation.updateCountryDependantValidation()
        sectionLocation.rebindCountryDependantFields()
        sectionLocation.updateStateFieldBasedOnBillingCountry(billingCountryCode)
    }

    private fun getBillingAddressCountryConfig(country: String): CountryConfig.BillingAddressCountryConfig {
        val billingCountryConfigs = CountryConfig.countryConfig.billingCountryConfigs
        val countryConfigNumber = CountryConfig.getCountryConfigId(country)
        return billingCountryConfigs[countryConfigNumber] as CountryConfig.BillingAddressCountryConfig
    }
}
