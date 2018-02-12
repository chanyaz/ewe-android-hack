package com.expedia.bookings.activity

import android.content.Context
import com.expedia.bookings.dagger.AppComponent
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.features.Plugins
import com.expedia.bookings.features.RemoteFeatureResolver
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
    override fun isEnabled(key: String): Boolean {
        return SatelliteFeatureConfigManager.isEnabled(context, key) || SettingUtils.get(context, key, false)
    }
}
