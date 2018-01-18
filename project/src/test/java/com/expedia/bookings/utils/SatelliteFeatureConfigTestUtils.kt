package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager

object SatelliteFeatureConfigTestUtils {

    @JvmStatic
    fun enableFeatureForTest(context: Context, featureString: String) {
        val editor = context.getSharedPreferences("featureConfig", Context.MODE_PRIVATE).edit()
        editor.putStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, setOf(featureString))
        editor.apply()
    }
}
