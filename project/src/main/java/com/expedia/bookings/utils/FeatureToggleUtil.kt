package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.mobiata.android.util.SettingUtils

class FeatureToggleUtil {
    companion object {
        @JvmStatic fun isUserBucketedAndFeatureEnabled(context: Context, abacusTestKey: Int, @StringRes featureKey: Int): Boolean {
            val isTestBucketed = isTestBucketed(abacusTestKey)
            val isFeatureEnabled = isFeatureEnabled(context, featureKey)
            return isTestBucketed && isFeatureEnabled
        }

        @JvmStatic fun isFeatureEnabled(context: Context, @StringRes featureKey: Int): Boolean {
            // enforcing everyone to clean the feature toggle before feature start showing in RC
            if (BuildConfig.RELEASE) return false
            val isFeatureEnabled = SettingUtils.get(context, featureKey, false)
            return isFeatureEnabled
        }

        private fun isTestBucketed(abacusTestKey: Int): Boolean {
            val isTestBucketed = Db.getAbacusResponse().isUserBucketedForTest(abacusTestKey)
            return isTestBucketed
        }

    }
}

