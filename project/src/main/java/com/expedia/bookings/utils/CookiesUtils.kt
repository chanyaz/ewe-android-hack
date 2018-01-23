package com.expedia.bookings.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.expedia.bookings.activity.RouterActivity
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.mobiata.android.util.SettingUtils

class CookiesUtils {

    companion object {

        val FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM = "oldCookiesMechanism"

        @JvmStatic
        fun shouldUseNewCookiesMechanism(context: Context): Boolean {
            return PointOfSale.getPointOfSale().shouldUseWebViewSyncCookieStore() || !SettingUtils.get(context, FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, false)
        }

        fun checkAndUpdateCookiesMechanism(context: Context) {
            val satelliteConfig = SatelliteFeatureConfigManager.isFeatureEnabled(context, FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM)
            val currentConfig = SettingUtils.get(context, FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, false)
            if (currentConfig != satelliteConfig) {
                SettingUtils.saveSynchronously(context, FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, satelliteConfig)
                restartApp(context)
            }
        }

        private fun restartApp(context: Context) {
            val mStartActivity = Intent(context, RouterActivity::class.java)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent
                    .getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            System.exit(0)
        }
    }
}
