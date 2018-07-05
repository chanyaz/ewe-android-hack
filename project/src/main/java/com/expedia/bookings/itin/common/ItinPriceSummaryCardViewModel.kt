package com.expedia.bookings.itin.common

import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasURLAnchor
import com.expedia.bookings.itin.scopes.HasWebViewLauncher

class ItinPriceSummaryCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasItinRepo, S : HasItinType, S : HasTripsTracking, S : HasURLAnchor {
    override val iconImage: Int = R.drawable.ic_itin_credit_card_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_price_summary_text)
    override val subheadingText: String? = null
    override val cardClickListener = {
        scope.itinRepo.liveDataItin.value?.let { itin ->
            val isGuest = itin.isGuest
            if (!itin.webDetailsURL.isNullOrEmpty() && !itin.tripId.isNullOrEmpty()) {
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_price_summary_text, itin.webDetailsURL!!, scope.urlAnchor, itin.tripId!!, isGuest = isGuest)
            }
        }
        scope.tripsTracking.trackItinLobPriceSummaryButtonClick(scope.type)
    }
}
