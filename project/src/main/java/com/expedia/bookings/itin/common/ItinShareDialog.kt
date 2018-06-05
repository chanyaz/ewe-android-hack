package com.expedia.bookings.itin.common

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.os.Build
import com.expedia.bookings.R
import com.expedia.bookings.itin.utils.ItinShareTargetBroadcastReceiver
import com.expedia.bookings.itin.utils.ShareItinTextCreator
import com.mobiata.android.util.SettingUtils

class ItinShareDialog(val context: Context) {

    fun showNativeShareDialog(shareText: String, tripType: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        shareIntent.type = "text/plain"

        SettingUtils.save(context, "TripType", tripType)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(shareIntent)
        } else {
            val receiver = Intent(context, ItinShareTargetBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooserIntent = Intent.createChooser(shareIntent, context.resources.getString(R.string.itin_share_dialog_title), pendingIntent.intentSender)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
            context.startActivity(chooserIntent)
        }
    }

    fun showItinShareDialog(shareItinTextCreator: ShareItinTextCreator, tripType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareItinTextCreator.getSmsBody())
        shareIntent.type = "text/plain"

        SettingUtils.save(context, "TripType", tripType)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(shareIntent)
        } else {
            val receiver = Intent(context, ItinShareTargetBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooserIntent = Intent.createChooser(shareIntent, context.resources.getString(R.string.itin_share_dialog_title), pendingIntent.intentSender)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val packageManager = context.packageManager
            val apps = packageManager.queryIntentActivities(shareIntent, 0)
            val emailIntents = ArrayList<LabeledIntent>()

            for (i in 0 until apps.size) {
                val app = apps[i]
                val packageName = app.activityInfo.packageName
                if (packageName.contains("android.email") || packageName.contains("android.gm")) {
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.component = ComponentName(packageName, app.activityInfo.name)
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, shareItinTextCreator.getEmailSubject())
                    emailIntent.putExtra(Intent.EXTRA_TEXT, shareItinTextCreator.getEmailBody())
                    emailIntent.type = "text/plain"
                    emailIntents.add(LabeledIntent(emailIntent, packageName, app.loadLabel(packageManager), app.icon))
                }
            }

            val extraIntents = emailIntents.toArray(arrayOfNulls<LabeledIntent>(emailIntents.size))

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
            context.startActivity(chooserIntent)
        }
    }
}
