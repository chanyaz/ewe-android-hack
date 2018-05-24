package com.expedia.bookings.utils

import android.content.Context
import android.content.SharedPreferences
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.features.Feature

object FeatureTestUtils {

    @JvmStatic
    fun enableFeature(context: Context, feature: Feature) {
        val prefs = getFeatureConfigPreferences(context)
        val supportedFeaturesSet = prefs.getStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, mutableSetOf())
        supportedFeaturesSet.add(feature.name)
        updatePrefFile(prefs, supportedFeaturesSet)
    }

    @JvmStatic
    fun disableFeature(context: Context, feature: Feature) {
        val prefs = getFeatureConfigPreferences(context)
        val supportedFeaturesSet = prefs.getStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, mutableSetOf())
        supportedFeaturesSet.remove(feature.name)
        updatePrefFile(prefs, supportedFeaturesSet)
    }

    private fun updatePrefFile(prefs: SharedPreferences, featureSet: Set<String>) {
        val editor = prefs.edit()
        editor.putStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, featureSet)
        editor.apply()
    }

    private fun getFeatureConfigPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SatelliteFeatureConfigManager.PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }
}
