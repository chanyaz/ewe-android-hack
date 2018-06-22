package com.expedia.bookings.utils

import android.graphics.Typeface
import android.widget.TextView

enum class Font(val typeface: Typeface) {
    ROBOTO_LIGHT(Typeface.create("sans-serif-light", Typeface.NORMAL)),
    ROBOTO_MEDIUM(Typeface.create("sans-serif-medium", Typeface.NORMAL)),
    ROBOTO_BOLD(Typeface.create("sans-serif", Typeface.BOLD)),
    ROBOTO_REGULAR(Typeface.create("sans-serif", Typeface.NORMAL));

    fun getTypefaceSpan(): TypefaceSpan {
        return TypefaceSpan(typeface)
    }

    fun setTypefaceOnTextView(textView: TextView) {
        if (!textView.isInEditMode) {
            textView.typeface = typeface
        }
    }
}
