package com.expedia.bookings.itin.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.activeandroid.Cache
import com.expedia.bookings.R
import com.expedia.bookings.itin.ItinShareTargetBroadcastReceiver
import com.mobiata.android.util.SettingUtils

class ShareItinDialog(val context: Context) {

    fun showNativeShareDialog(shareText: String, tripType: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        shareIntent.type = "text/plain"

        SettingUtils.save(Cache.getContext(), "TripType", tripType)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(shareIntent)
        } else {
            val receiver = Intent(Cache.getContext(), ItinShareTargetBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(Cache.getContext(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooserIntent = Intent.createChooser(shareIntent, context.resources.getString(R.string.itin_share_dialog_title), pendingIntent.intentSender)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
            Cache.getContext().startActivity(chooserIntent)
        }
    }
}
