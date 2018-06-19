package com.expedia.bookings.itin.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.expedia.bookings.content.ShareTargetBroadcastReceiver
import com.expedia.bookings.tracking.TripsTracking

class NewItinShareTargetBroadcastReceiver : ShareTargetBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val tripType = intent.extras.get(Intent.EXTRA_KEY_EVENT).toString()
            val shareTarget = intent.extras.get(Intent.EXTRA_CHOSEN_COMPONENT).toString().substringAfter("{").substringBefore("/")
            onShareTargetReceived(tripType, shareTarget)
        }
    }

    override fun onShareTargetReceived(tripType: String, shareTarget: String) {
        TripsTracking.trackShareItinFromApp(tripType, shareTarget)
    }
}
