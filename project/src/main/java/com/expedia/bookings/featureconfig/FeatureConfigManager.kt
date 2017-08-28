package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.text.format.DateUtils


class FeatureConfigManager(val context: Context) {
    private val PREFS_FILE_NAME = "featureConfig"
    private val PREFS_FEATURE_CONFIG_LAST_UPDATED = "lastUpdated"
    private val PREFS_SUPPORTED_FEATURE_SET = "supportedFeatures"
    private val FEATURE_CONFIG_REFRESH_TIMEOUT = DateUtils.DAY_IN_MILLIS

    private var viewModel: SatelliteFeatureConfigViewModel = SatelliteFeatureConfigViewModel()

    init {
        viewModel.featureConfigResponseObservable.subscribe { response ->
            cacheFeatureConfig(response)
        }
    }

    fun refreshFeatureConfigIfStale() {
        if (shouldUpdateConfig()) {
            viewModel.fetchRemoteConfig()
        }
    }

    @VisibleForTesting
    fun shouldUpdateConfig(): Boolean {
        val prefs = getFeatureConfigPreferences()
        val timeSinceLastUpdate = System.currentTimeMillis() - prefs.getLong(PREFS_FEATURE_CONFIG_LAST_UPDATED, 0)
        return timeSinceLastUpdate > FEATURE_CONFIG_REFRESH_TIMEOUT || timeSinceLastUpdate < 0
    }

    @VisibleForTesting
    fun cacheFeatureConfig(supportedFeatureIds: List<String>) {
        val editor = getFeatureConfigPreferences().edit()
        editor.putStringSet(PREFS_SUPPORTED_FEATURE_SET, supportedFeatureIds.toSet())
        editor.putLong(PREFS_FEATURE_CONFIG_LAST_UPDATED, System.currentTimeMillis())
        editor.apply()
    }


    private fun getFeatureConfigPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }
}