package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.utils.CookiesUtils
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit

class SatelliteFeatureConfigManager {

    companion object {
        const val PREFS_FEATURE_CONFIG_LAST_UPDATED = "lastUpdated"
        const val PREFS_SUPPORTED_FEATURE_SET = "supportedFeatures"
        const val PREFS_FILE_NAME = "featureConfig"
        private val FEATURE_CONFIG_REFRESH_TIMEOUT = TimeUnit.HOURS.toMillis(4)
        private val FEATURE_CONFIG_VALID_TIMEOUT = TimeUnit.HOURS.toMillis(6)

        @JvmStatic fun forceRefreshFeatureConfig(context: Context) {
            clearFeatureConfig(context)
            refreshFeatureConfigIfStale(context)
        }

        @JvmStatic fun refreshFeatureConfigIfStale(context: Context) {
            if (shouldUpdateConfig(context)) {
                fetchRemoteConfig(context)
            }
        }

        @JvmStatic fun isFeatureEnabled(context: Context, featureString: String): Boolean {
            return isEnabledFetchIfStale(context, featureString)
        }

        @JvmStatic fun isABTestEnabled(context: Context, abacusTestId: Int): Boolean {
            return isEnabledFetchIfStale(context, abacusTestId.toString())
        }

        @VisibleForTesting
        @JvmStatic fun shouldUpdateConfig(context: Context): Boolean {
            val prefs = getFeatureConfigPreferences(context)
            val timeSinceLastUpdate = System.currentTimeMillis() - prefs.getLong(PREFS_FEATURE_CONFIG_LAST_UPDATED, 0)
            return timeSinceLastUpdate > FEATURE_CONFIG_REFRESH_TIMEOUT || timeSinceLastUpdate < 0
        }

        @VisibleForTesting
        @JvmStatic fun cacheFeatureConfig(context: Context, supportedFeatureIds: List<String>) {
            val editor = getFeatureConfigPreferences(context).edit()
            editor.putStringSet(PREFS_SUPPORTED_FEATURE_SET, supportedFeatureIds.toSet())
            editor.putLong(PREFS_FEATURE_CONFIG_LAST_UPDATED, System.currentTimeMillis())
            editor.apply()
        }

        @VisibleForTesting
        @JvmStatic fun configValid(context: Context): Boolean {
            val prefs = getFeatureConfigPreferences(context)
            val timeSinceLastUpdate = System.currentTimeMillis() - prefs.getLong(PREFS_FEATURE_CONFIG_LAST_UPDATED, 0)

            return timeSinceLastUpdate < FEATURE_CONFIG_VALID_TIMEOUT || timeSinceLastUpdate < 0
        }

        @VisibleForTesting
        @JvmStatic fun isEnabled(context: Context, featureString: String): Boolean {
            val prefs = getFeatureConfigPreferences(context)
            val supportedFeatures = prefs.getStringSet(PREFS_SUPPORTED_FEATURE_SET, emptySet())
            return supportedFeatures.contains(featureString)
        }

        @JvmStatic fun clearFeatureConfig(context: Context) {
            val editor = getFeatureConfigPreferences(context).edit()
            editor.clear().apply()
        }

        private fun isEnabledFetchIfStale(context: Context, featureString: String): Boolean {
            val default = false
            if (ExpediaBookingApp.isAutomation() || configValid(context)) {
                return isEnabled(context, featureString)
            } else {
                fetchRemoteConfig(context)
            }
            return default
        }

        private fun fetchRemoteConfig(context: Context) {
            if (!ExpediaBookingApp.isAutomation()) {
                val satelliteServices = Ui.getApplication(context).appComponent().satelliteServices()
                satelliteServices.fetchFeatureConfig(createConfigResponseObserver(context))
            }
        }

        private fun getFeatureConfigPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }

        private fun createConfigResponseObserver(context: Context): Observer<List<String>> {
            return object : DisposableObserver<List<String>>() {
                override fun onNext(featureConfigResponse: List<String>) {
                    cacheFeatureConfig(context, featureConfigResponse)
                    CookiesUtils.checkAndUpdateCookiesMechanism(context)
                }

                override fun onComplete() {}

                override fun onError(e: Throwable) {
                    Log.e("Satellite Feature Config Fetch Error", e)
                }
            }
        }
    }
}
