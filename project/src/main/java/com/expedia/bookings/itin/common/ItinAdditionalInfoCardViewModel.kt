package com.expedia.bookings.itin.common

import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher

class ItinAdditionalInfoCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasItin {
    override val iconImage: Int = R.drawable.ic_itin_additional_info_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_hotel_details_additional_info_heading)
    override val subheadingText: String? = null
    override val cardClickListener = {
        val itin = scope.itin
        val isGuest = itin.isGuest
        if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
            scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_additional_info_heading, itin.webDetailsURL!!, null, itin.tripId!!, isGuest = isGuest)
        }
    }
}
