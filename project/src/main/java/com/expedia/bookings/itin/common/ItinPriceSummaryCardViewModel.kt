package com.expedia.bookings.itin.common

import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher

class ItinPriceSummaryCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasItin {
    override val iconImage: Int = R.drawable.ic_itin_credit_card_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_price_summary_heading)
    override val subheadingText: String? = null
    override val cardClickListener = {
        val itin = scope.itin
            val isGuest = itin.isGuest
            if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, itin.webDetailsURL!!, "price", itin.tripId!!, isGuest = isGuest)
            }
        }
}
