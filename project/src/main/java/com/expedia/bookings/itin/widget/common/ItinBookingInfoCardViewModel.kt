package com.expedia.bookings.itin.widget.common

interface ItinBookingInfoCardViewModel {
    val iconImage: Int
    val headingText: String
    val subheadingText: String?
    val cardClickListener: () -> Unit
}
