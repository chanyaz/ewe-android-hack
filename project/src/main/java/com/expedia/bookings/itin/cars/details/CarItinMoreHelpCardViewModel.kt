package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.scopes.HasStringProvider

class CarItinMoreHelpCardViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasStringProvider {
    override val iconImage: Int = R.drawable.ic_itin_manage_booking_icon
    override val headingText: String = scope.strings.fetch(R.string.itin_lx_more_info_heading)
    override val subheadingText: String? = scope.strings.fetch(R.string.itin_lx_more_info_subheading)
    override val cardClickListener: () -> Unit = {
        TODO("not implemented") //add native itin manage booking activity here
    }
}