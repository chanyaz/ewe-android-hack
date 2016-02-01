package com.expedia.bookings.utils

import android.text.InputFilter
import android.text.Spanned
import java.text.DecimalFormatSymbols
import java.util.Locale

class DecimalNumberInputFilter(val decimalDigits: Int) : InputFilter {

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val decimalSeparator = DecimalFormatSymbols(Locale.US).decimalSeparator
        val split = dest.split(decimalSeparator)
        if (split.size == 2) {
            if(split[1].length >= decimalDigits) return ""
        }
        return null
    }
}