package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.lx.moreHelp.LxItinMoreHelpActivity
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking

class LxItinMoreHelpCardViewModel<S>(val scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasActivityLauncher, S : HasItinRepo, S : HasTripsTracking {
    override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_more_help_text)
    override val subheadingText: String? = scope.strings.fetch(R.string.itin_customer_support_info_text)
    override val cardClickListener: () -> Unit = {
        val itin = scope.itinRepo.liveDataItin.value!!
        itin.tripId?.let { tripId ->
            scope.activityLauncher.launchActivity(LxItinMoreHelpActivity, tripId)
            scope.tripsTracking.trackItinLxMoreHelpClicked()
        }
    }
}
