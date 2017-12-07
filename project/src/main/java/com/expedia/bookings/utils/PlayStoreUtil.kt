package com.expedia.bookings.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

class PlayStoreUtil {

    companion object {
        @JvmStatic
        fun openPlayStore(mActivity: Activity) {

            val uri = Uri.parse("market://details?id=" + mActivity.getPackageName())
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)

            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            try {
                mActivity.startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                mActivity.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + mActivity.getPackageName())))
            }

        }
    }
}