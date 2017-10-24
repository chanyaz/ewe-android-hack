package com.expedia.bookings.utils.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.ui.PackageActivity

class PackageNavUtils : NavUtils() {
    companion object {
        @JvmStatic fun goToPackages(context: Context, data: Bundle?, animOptions: Bundle?, expediaFlags: Int = 0) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, PackageActivity::class.java)
            if (data != null) {
                intent.putExtras(data)
            }
            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }

        @JvmStatic fun goToPackagesForResult(context: Context, data: Bundle?, animOptions: Bundle?, requestCode: Int) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, PackageActivity::class.java)
            if (data != null) {
                intent.putExtras(data)
            }
            startActivityForResult(context, intent, animOptions, requestCode)
        }

        @JvmStatic fun goToPackagesForReplaySearch(context: Context, data: Bundle?, animOptions: Bundle?) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, PackageActivity::class.java)
            if (data != null) {
                intent.putExtras(data)
            }
            startActivity(context, intent, animOptions)
        }
    }
}