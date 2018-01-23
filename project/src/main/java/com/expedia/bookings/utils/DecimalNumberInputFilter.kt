package com.expedia.bookings.utils

import android.text.InputFilter
import android.text.Spanned
import java.text.DecimalFormatSymbols
import java.util.Locale

class DecimalNumberInputFilter(val decimalDigits: Int) : InputFilter {

    var finalInput = StringBuilder()

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {

        if (start == end) return null

        finalInput.setLength(0)
        finalInput.append(dest.toString())
        finalInput.insert(dstart, source)

        val decimalSeparator = DecimalFormatSymbols(Locale.US).decimalSeparator
        val split = finalInput.split(decimalSeparator)
        if (split.size == 2) {
            if (split[1].length > decimalDigits) return ""
        }
        return null
    }
}
