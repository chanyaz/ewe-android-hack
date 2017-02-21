package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Db
import com.mobiata.android.util.SettingUtils

class FeatureToggleUtil {
    companion object {
        @JvmStatic fun isUserBucketedAndFeatureEnabled(context: Context, abacusTestKey: Int, featureKey: Int): Boolean {
            val isTestBucketed = isTestBucketed(abacusTestKey)
            val isFeatureEnabled = isFeatureEnabled(context, featureKey)
            return isTestBucketed && isFeatureEnabled
        }

        @JvmStatic fun isFeatureEnabled(context: Context, featureKey: Int): Boolean {
            // enforcing everyone to clean the feature toggle before feature start showing in RC
            if (BuildConfig.RELEASE) return false
            val isFeatureEnabled = SettingUtils.get(context, featureKey, false)
            return isFeatureEnabled
        }

        @JvmStatic fun enableFeatureOnDebugBuild(context: Context, featureKey: Int) {
            if (BuildConfig.DEBUG) {
                SettingUtils.save(context, featureKey, true)
            }
        }

        private fun isTestBucketed(abacusTestKey: Int): Boolean {
            val isTestBucketed = Db.getAbacusResponse().isUserBucketedForTest(abacusTestKey)
            return isTestBucketed
        }

    }
}

