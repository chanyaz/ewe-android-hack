package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.mobiata.android.util.SettingUtils

class FeatureToggleUtil {
    companion object {
        @JvmStatic fun isUserBucketedAndFeatureEnabled(context: Context, abTest: ABTest, @StringRes featureKey: Int): Boolean {
            val isTestBucketed = isTestBucketed(context, abTest)
            val isFeatureEnabled = isFeatureEnabled(context, featureKey)
            return isTestBucketed && isFeatureEnabled
        }

        @JvmStatic fun isFeatureEnabled(context: Context, @StringRes featureKey: Int): Boolean {
            // enforcing everyone to clean the feature toggle before feature start showing in RC
            if (BuildConfig.RELEASE) return false
            return SettingUtils.get(context, featureKey, false)
        }

        private fun isTestBucketed(context: Context, abTest: ABTest): Boolean {
            return AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest)
        }
    }
}
