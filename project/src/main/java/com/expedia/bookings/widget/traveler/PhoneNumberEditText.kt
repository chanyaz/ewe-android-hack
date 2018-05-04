package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.autofill.AutofillValue
import com.expedia.bookings.utils.CountryCodeUtil

class PhoneNumberEditText(context: Context, attrs: AttributeSet?) : TravelerEditText(context, attrs) {

    override fun getAutofillValue(value: AutofillValue?): AutofillValue? {
        return AutofillValue.forText(getPhoneNumberWithoutCountryCode(value))
    }

    private fun getPhoneNumberWithoutCountryCode(value: AutofillValue?): String {
        var phoneNumber = value?.textValue?.toString()
        phoneNumber?.let { number ->
            val countryCode = CountryCodeUtil.getCountryCode(number)
            if (countryCode.isNotEmpty()) {
                val countryCodeWithPlus = number.substring(0, 1) == "+"
                phoneNumber = number.replaceFirst(countryCode, "")
                phoneNumber = if (countryCodeWithPlus) phoneNumber!!.replaceFirst("+", "") else phoneNumber
            }
        }
        return phoneNumber ?: ""
    }
}
