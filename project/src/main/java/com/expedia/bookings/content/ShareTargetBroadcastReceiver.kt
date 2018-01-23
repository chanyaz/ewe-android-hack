package com.expedia.bookings.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobiata.android.util.SettingUtils

abstract class ShareTargetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tripType = SettingUtils.get(context, "TripType", null)
        for (key in intent.extras.keySet()) {
            val shareTarget = intent.extras.get(key).toString().substringAfter("{").substringBefore("/")
            onShareTargetReceived(tripType, shareTarget)
        }
    }

    protected abstract fun onShareTargetReceived(tripType: String, shareTarget: String)
}
