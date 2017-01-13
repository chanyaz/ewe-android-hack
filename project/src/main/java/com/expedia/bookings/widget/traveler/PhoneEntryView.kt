package com.expedia.bookings.widget.traveler

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.TelephoneSpinner
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.vm.traveler.TravelerPhoneViewModel

class PhoneEntryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val phoneSpinner: TelephoneSpinner by bindView(R.id.edit_phone_number_country_code_spinner)
    val phoneNumber: TravelerEditText by bindView(R.id.edit_phone_number)
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)

    var isFirstSelected = false

    var viewModel: TravelerPhoneViewModel by notNullAndObservable { vm ->
        phoneNumber.viewModel = vm.phoneViewModel
        vm.phoneCountryCodeSubject.subscribe { countryCode ->
            if (!TextUtils.isEmpty(countryCode)) {
                phoneSpinner.update(countryCode, "")
            } else {
                phoneSpinner.selectPOSCountry()
            }
        }
        phoneSpinner.onItemSelectedListener = PhoneSpinnerItemSelected()
        spinnerUpdated()

        if (materialFormTestEnabled) {
            phoneNumber.subscribeMaterialFormsError(phoneNumber.viewModel.errorSubject, context.getString(R.string.phone_validation_error_message))
        }
    }

    init {
        if (materialFormTestEnabled) {
            View.inflate(context, R.layout.material_phone_entry_view, this)
        } else {
            View.inflate(context, R.layout.phone_entry_view, this)
            gravity = Gravity.BOTTOM
        }
        orientation = HORIZONTAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val isExtraPaddingRequired = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
        if (isExtraPaddingRequired) {
            val editTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.checkout_earlier_api_version_edit_text_spacing)
            phoneNumber.setPadding(phoneNumber.paddingLeft, phoneNumber.paddingTop, phoneNumber.paddingRight, editTextSpacing)
        }
    }

    private fun spinnerUpdated() {
        viewModel.countryCodeObserver.onNext(phoneSpinner.selectedTelephoneCountryCode)
        viewModel.countryNameObserver.onNext(phoneSpinner.selectedTelephoneCountry)
    }

    private inner class PhoneSpinnerItemSelected() : AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            spinnerUpdated()
            phoneSpinner.updateText()
            if (isFirstSelected) {
                view?.setAccessibilityHoverFocus()
            } else {
                isFirstSelected = true
            }
        }
    }
}