package com.expedia.bookings.extensions

import android.widget.LinearLayout
import com.expedia.bookings.widget.LabeledCheckableFilter

fun LinearLayout.clearChecks() {
    for (i in 0..childCount - 1) {
        val v = getChildAt(i)
        if (v is LabeledCheckableFilter<*> && v.checkBox.isChecked) {
            v.checkBox.isChecked = false
        }
    }
}
