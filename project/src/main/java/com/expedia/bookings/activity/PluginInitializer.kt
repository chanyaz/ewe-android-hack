package com.expedia.bookings.activity

import android.content.Context
import com.expedia.bookings.dagger.AppComponent
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.features.RemoteFeatureResolver
import com.expedia.bookings.plugins.Plugins
import com.mobiata.android.util.SettingUtils

class PluginInitializer {
    companion object {
        @JvmStatic
        fun initializePlugins(appComponent: AppComponent) {
            Plugins.remoteFeatureResolver = appComponent.satelliteRemoteFeatureResolver()
        }
    }
}

class SatelliteRemoteFeatureResolver(private val context: Context) : RemoteFeatureResolver {
    private val preferenceOverrideOnKey = "remoteFeatures-localOverride-On"
    private val preferenceOverrideOffKey = "remoteFeatures-localOverride-Off"

    override fun isEnabled(key: String): Boolean {
        val overrideOnSet = SettingUtils.getStringSet(context, preferenceOverrideOnKey)
        val turnedOn = SatelliteFeatureConfigManager.isEnabled(context, key) || overrideOnSet.contains(key)
        val notTurnedOff = !SettingUtils.getStringSet(context, preferenceOverrideOffKey).contains(key)
        return turnedOn && notTurnedOff
    }
}
