package com.expedia.bookings.itin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.util.SettingUtils

class ItinShareTargetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tripType = SettingUtils.get(context, "TripType", null)
        for (key in intent.extras.keySet()) {
            val shareTarget = intent.extras.get(key).toString().substringAfter("{").substringBefore("/")
            OmnitureTracking.trackItinShareAppChosen(tripType, shareTarget)
        }
    }
}