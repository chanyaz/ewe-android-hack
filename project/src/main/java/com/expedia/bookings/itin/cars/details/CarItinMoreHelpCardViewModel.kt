package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking

class CarItinMoreHelpCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider, S : HasCarRepo, S : HasActivityLauncher, S : HasTripsTracking {
    override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
    override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
    override val cardClickListener: () -> Unit = {
        val itin = scope.itinCarRepo.liveDataItin.value!!
        itin.tripId?.let { tripId ->
            scope.tripsTracking.trackItinCarMoreHelpClicked()
            scope.activityLauncher.launchActivity(CarItinMoreHelpActivity, tripId)
        }
    }
}
