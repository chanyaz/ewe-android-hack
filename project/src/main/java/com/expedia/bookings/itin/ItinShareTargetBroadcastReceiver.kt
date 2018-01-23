package com.expedia.bookings.itin

import com.expedia.bookings.content.ShareTargetBroadcastReceiver
import com.expedia.bookings.tracking.OmnitureTracking

class ItinShareTargetBroadcastReceiver : ShareTargetBroadcastReceiver() {
    override fun onShareTargetReceived(tripType: String, shareTarget: String) {
        OmnitureTracking.trackItinShareAppChosen(tripType, shareTarget)
    }
}
