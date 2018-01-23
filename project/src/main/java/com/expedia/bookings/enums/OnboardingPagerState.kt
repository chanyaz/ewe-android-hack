package com.expedia.bookings.enums

import com.expedia.bookings.R
import com.expedia.bookings.utils.Constants

enum class OnboardingPagerState(val titleResId: Int, val subtitleResId: Int, val backgroundUrl: String, val placeholderResId: Int) {
    BOOKING_PAGE(R.string.booking_page_title, R.string.booking_page_subtitle, Constants.ONBOARDING_BOOKING_PAGE_URL, R.drawable.onboarding_booking_placeholder),
    TRIP_PAGE(R.string.trip_page_title, R.string.trip_page_subtitle, Constants.ONBOARDING_TRIP_PAGE_URL, R.drawable.onboarding_trip_placeholder),
    REWARD_PAGE(R.string.reward_page_title, R.string.reward_page_subtitle_TEMPLATE, Constants.ONBOARDING_REWARD_PAGE_URL, R.drawable.onboarding_reward_placeholder)
}
