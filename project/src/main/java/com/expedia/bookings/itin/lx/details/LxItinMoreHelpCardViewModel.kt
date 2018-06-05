package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.lx.moreHelp.LxItinMoreHelpActivity
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking

class LxItinMoreHelpCardViewModel<S>(val scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasActivityLauncher, S : HasLxRepo, S : HasTripsTracking {
    override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
    override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
    override val cardClickListener: () -> Unit = {
        val itin = scope.itinLxRepo.liveDataItin.value!!
        itin.tripId?.let { tripId ->
            scope.activityLauncher.launchActivity(LxItinMoreHelpActivity, tripId)
            scope.tripsTracking.trackItinLxMoreHelpClicked()
        }
    }
}
