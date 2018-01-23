package com.expedia.bookings.enums

import com.expedia.bookings.R

enum class DiscountColors(val backgroundColor: Int, val textColor: Int) {
    MEMBER_DEALS(R.drawable.member_only_discount_percentage_background, R.color.brand_primary),
    LAST_MINUTE_DEALS(R.drawable.discount_percentage_background, R.color.white)
}
