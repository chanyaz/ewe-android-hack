package com.expedia.bookings.widget.accessibility

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import com.expedia.bookings.R
import com.expedia.bookings.extensions.getParentTextInputLayout
import com.expedia.bookings.utils.CountryCodeUtil

class AccessibleEditTextForSpinner(context: Context, attributeSet: AttributeSet) : AccessibleEditText(context, attributeSet) {

    private var autoFillManager: AutofillManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            autoFillManager = context.getSystemService(AutofillManager::class.java)
        }
    }

    override fun getAccessibilityNodeInfo(): String {
        val accessibilityString: String
        if (!valid) {
            val hint = this.getParentTextInputLayout()?.hint ?: this.hint?.toString() ?: ""
            val error = this.getParentTextInputLayout()?.error ?: errorMessage
            val openDialogHint = context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
            accessibilityString = "$hint, $openDialogHint, $defaultErrorString, $error"
        } else {
            val text = this.text.toString()
            val hint = this.getParentTextInputLayout()?.hint ?: this.hint?.toString() ?: ""
            if (text.isEmpty()) {
                accessibilityString = " $hint" + ", " + context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
            } else {
                accessibilityString = " $hint, $text" + ", " + context.resources.getString(R.string.accessibility_cont_desc_opens_dialog)
            }
        }
        return accessibilityString
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.text = getAccessibilityNodeInfo()
    }

    override fun autofill(value: AutofillValue?) {
        var autofillValue =
        if (id == R.id.material_edit_phone_number_country_code) {
            getAutofillValueWithCountryCode(value)
        } else {
            value
        }
        super.autofill(autofillValue)
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
