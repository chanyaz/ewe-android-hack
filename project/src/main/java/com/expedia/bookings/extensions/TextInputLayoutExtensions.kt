package com.expedia.bookings.extensions

import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatTextView
import android.view.View
import com.expedia.bookings.R

fun TextInputLayout.hideErrorTextViewFromHoverFocus() {
    val errorText = this.findViewById<AppCompatTextView>(R.id.textinput_error)
    errorText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
}
