package com.expedia.bookings.data.hotel

import android.support.annotation.StringRes
import com.expedia.bookings.R

enum class Sort(@StringRes val resId: Int) {
    RECOMMENDED(R.string.recommended),
    PRICE(R.string.price),
    DEALS(R.string.sort_description_deals),
    PACKAGE_DISCOUNT(R.string.sort_description_package_discount),
    RATING(R.string.rating),
    DISTANCE(R.string.distance);
}