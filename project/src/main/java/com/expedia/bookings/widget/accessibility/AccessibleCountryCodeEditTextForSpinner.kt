package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.util.AttributeSet
import android.view.autofill.AutofillValue
import com.expedia.bookings.utils.CountryCodeUtil

class AccessibleCountryCodeEditTextForSpinner(context: Context, attributeSet: AttributeSet) : AccessibleEditTextForSpinner(context, attributeSet) {

    override fun getAutofillValue(value: AutofillValue?): AutofillValue? {
        return getAutofillValueWithCountryCode(value)
    }

    private fun getAutofillValueWithCountryCode(value: AutofillValue?): AutofillValue? {
        val countryCode = CountryCodeUtil.getCountryCode(value?.textValue?.toString() ?: "")
        return if (countryCode.isEmpty()) {
            AutofillValue.forText(this.text.toString())
        } else {
            AutofillValue.forText("+$countryCode")
        }
    }
}
