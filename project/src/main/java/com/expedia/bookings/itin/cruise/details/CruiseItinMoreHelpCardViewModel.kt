package com.expedia.bookings.itin.cruise.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasURLAnchor
import com.expedia.bookings.itin.scopes.HasWebViewLauncher

class CruiseItinMoreHelpCardViewModel<S>(val scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasItinRepo, S : HasWebViewLauncher, S : HasTripsTracking, S : HasURLAnchor {
    override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_more_help_text)
    override val subheadingText: String? = scope.strings.fetch(R.string.itin_customer_support_info_text)
    override val cardClickListener: () -> Unit = {
        scope.itinRepo.liveDataItin.value?.let { itin ->
            val isGuest = itin.isGuest
            if (itin.webDetailsURL != null && itin.tripId != null) {
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_more_help_text, itin.webDetailsURL, scope.urlAnchor, itin.tripId, isGuest = isGuest)
            }
        }
    }
}
