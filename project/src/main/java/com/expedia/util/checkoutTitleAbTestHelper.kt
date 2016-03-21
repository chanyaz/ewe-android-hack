package com.expedia.util

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

fun getCheckoutToolbarTitle(resources: Resources): String {
    val isUseSecureCheckoutTitle = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)
    val resId = if (isUseSecureCheckoutTitle) R.string.secure_checkout else R.string.cars_checkout_text
    return resources.getString(resId)
}
