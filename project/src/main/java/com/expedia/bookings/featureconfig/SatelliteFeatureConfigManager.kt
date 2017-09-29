package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.text.format.DateUtils
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import rx.Observer

class SatelliteFeatureConfigManager {

    companion object {
        const val PREFS_FEATURE_CONFIG_LAST_UPDATED = "lastUpdated"
        const val PREFS_SUPPORTED_FEATURE_SET = "supportedFeatures"
        private const val PREFS_FILE_NAME = "featureConfig"
        private const val FEATURE_CONFIG_REFRESH_TIMEOUT = 4 * DateUtils.HOUR_IN_MILLIS   //refresh the cache every 4 hours
        private const val FEATURE_CONFIG_VALID_TIMEOUT = 6 * DateUtils.HOUR_IN_MILLIS     //honor cache for 6 hours

        @JvmStatic fun forceRefreshFeatureConfig(context: Context) {
            clearFeatureConfig(context)
            refreshFeatureConfigIfStale(context)
        }

        @JvmStatic fun refreshFeatureConfigIfStale(context: Context) {
            if (shouldUpdateConfig(context)) {
                fetchRemoteConfig(context)
            }
        }

        @JvmStatic fun isABTestEnabled(context: Context, abacusTestId: Int): Boolean {
            return isTestEnabledFetchIfStale(context, abacusTestId)
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
        @JvmStatic fun testEnabled(context: Context, abacusTestId: Int): Boolean {
            val prefs = getFeatureConfigPreferences(context)
            val supportedFeatures = prefs.getStringSet(PREFS_SUPPORTED_FEATURE_SET, emptySet())
            return supportedFeatures.contains(abacusTestId.toString())
        }

        private fun clearFeatureConfig(context: Context) {
            val editor = getFeatureConfigPreferences(context).edit()
            editor.clear().apply()
        }

        private fun isTestEnabledFetchIfStale(context: Context, abacusTestId: Int): Boolean {
            val default = false
            if (configValid(context)) {
                return testEnabled(context, abacusTestId)
            } else {
                fetchRemoteConfig(context)
            }
            return default
        }

        private fun fetchRemoteConfig(context: Context) {
            val satelliteServices = Ui.getApplication(context).appComponent().satelliteServices()
            satelliteServices.fetchFeatureConfig(createConfigResponseObserver(context))
        }

        private fun getFeatureConfigPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }

        private fun createConfigResponseObserver(context: Context): Observer<List<String>> {
            return object : Observer<List<String>> {
                override fun onNext(featureConfigResponse: List<String>) {
                    cacheFeatureConfig(context, featureConfigResponse)
                }

                override fun onCompleted() {}

                override fun onError(e: Throwable?) {
                    Log.e("Satellite Feature Config Fetch Error", e)
                }
            }
        }
    }
}