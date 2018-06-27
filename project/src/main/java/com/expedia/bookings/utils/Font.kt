package com.expedia.bookings.utils

import android.graphics.Typeface
import android.widget.TextView
import com.expedia.bookings.extensions.setTypeface

enum class Font(val typeface: Typeface) {
    ROBOTO_LIGHT(Typeface.create("sans-serif-light", Typeface.NORMAL)),
    ROBOTO_MEDIUM(Typeface.create("sans-serif-medium", Typeface.NORMAL)),
    ROBOTO_BOLD(Typeface.create("sans-serif", Typeface.BOLD)),
    ROBOTO_REGULAR(Typeface.create("sans-serif", Typeface.NORMAL));

    fun getTypefaceSpan(): TypefaceSpan {
        return TypefaceSpan(typeface)
    }

    fun setTypefaceOnTextView(textView: TextView) {
        textView.setTypeface(this)
    }
}
