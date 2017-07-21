package com.expedia.bookings.widget.flights

import com.expedia.bookings.content.ShareTargetBroadcastReceiver
import com.expedia.bookings.tracking.OmnitureTracking

class FlightConfirmationShareBroadcastReceiver: ShareTargetBroadcastReceiver() {
    override fun onShareTargetReceived(tripType: String, shareTarget: String) {
        OmnitureTracking.trackFlightConfirmationShareAppChosen(tripType, shareTarget)
    }
}
