package com.expedia.bookings.itin.common

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.net.Uri
import android.os.Build
import com.activeandroid.Cache
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

        SettingUtils.save(Cache.getContext(), "TripType", tripType)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
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

    fun showItinShareDialog(shareItinTextCreator: ShareItinTextCreator, tripType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        SettingUtils.save(context, "TripType", tripType)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareItinTextCreator.getSmsBody())
            context.startActivity(shareIntent)
        } else {
            val packageManager = context.packageManager

            // Get all email-app package names
            val emailIntent = Intent(Intent.ACTION_VIEW)
            emailIntent.data = Uri.parse("mailto:")
            val emailApps = packageManager.queryIntentActivities(emailIntent, 0)
            val emailAppPackageNames = HashSet<String>()
            for (app in emailApps) {
                emailAppPackageNames.add(app.activityInfo.packageName)
            }

            // Get a list of all shareable-apps
            val allSharableApps = packageManager.queryIntentActivities(shareIntent, 0)
            val intents = ArrayList<LabeledIntent>()

            // Go through each shareable-app
            for (app in allSharableApps) {
                val packageName = app.activityInfo.packageName
                val intent = Intent(Intent.ACTION_SEND)
                intent.component = ComponentName(packageName, app.activityInfo.name)

                // If app is an email app, add email content to intent; otherwise, sms content and store in chooserIntent
                if (emailAppPackageNames.contains(packageName)) {
                    intent.putExtra(Intent.EXTRA_SUBJECT, shareItinTextCreator.getEmailSubject())
                    intent.putExtra(Intent.EXTRA_TEXT, shareItinTextCreator.getEmailBody())
                    intent.type = "message/rfc822"
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, shareItinTextCreator.getSmsBody())
                    intent.type = "text/plain"
                }
                intents.add(LabeledIntent(intent, packageName, app.loadLabel(packageManager), app.icon))
            }

            val appIntents = intents.toArray(arrayOfNulls<LabeledIntent>(intents.size))

            val receiver = Intent(context, ItinShareTargetBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooserIntent = Intent.createChooser(Intent(), context.resources.getString(R.string.itin_share_dialog_title), pendingIntent.intentSender)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, appIntents)
            context.startActivity(chooserIntent)
        }
    }
}
