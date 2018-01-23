package com.expedia.util

import android.view.View
import com.expedia.bookings.widget.TextView

fun View.setInverseVisibility(forward: Boolean) {
    this.visibility = if (forward) View.GONE else View.VISIBLE
}

fun TextView.setInverseVisibility(forward: Boolean) {
    this.visibility = if (!forward && this.text.isNotEmpty()) View.VISIBLE else View.GONE
}

fun TextView.setTextAndVisibility(text: CharSequence?) {
    this.text = text ?: ""
    setInverseVisibility(text.isNullOrBlank())
}

fun View.updateVisibility(show: Boolean) {
    this.visibility = if (show) View.VISIBLE else View.GONE
}

