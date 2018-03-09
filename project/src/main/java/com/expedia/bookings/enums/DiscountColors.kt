package com.expedia.bookings.enums

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.expedia.bookings.R

enum class DiscountColors(@DrawableRes val backgroundDrawableResId: Int, @ColorRes val textColorResId: Int) {
    DEFAULT(0, R.color.white),
    MEMBER_DEALS(R.drawable.member_only_discount_percentage_background, R.color.brand_primary),
    LAST_MINUTE_DEALS(R.drawable.discount_percentage_background, R.color.white)
}
