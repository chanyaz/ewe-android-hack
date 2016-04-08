package com.expedia.util

import android.content.res.Resources
import com.expedia.bookings.R

fun getCheckoutToolbarTitle(res: Resources, isSecureToolbarTestBucketed: Boolean): String {
    if (isSecureToolbarTestBucketed) {
        return res.getString(R.string.secure_checkout)
    } else {
        return res.getString(R.string.checkout_text)
    }
}