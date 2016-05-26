package com.expedia.util

import android.content.res.Resources
import com.expedia.bookings.R
import com.squareup.phrase.Phrase

fun getCheckoutToolbarTitle(res: Resources, isSecureToolbarTestBucketed: Boolean): String {
    if (isSecureToolbarTestBucketed) {
        return res.getString(R.string.secure_checkout)
    } else {
        return res.getString(R.string.checkout_text)
    }
}

fun getMainTravelerToolbarTitle(res: Resources): String {
    val title = Phrase.from(res.getString(R.string.checkout_edit_traveler_TEMPLATE))
            .put("travelernumber", 1)
            .put("passengerage", res.getString(R.string.ticket_type_adult))
            .format().toString()
    return title
}