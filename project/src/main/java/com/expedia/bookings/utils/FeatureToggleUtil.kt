package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Db
import com.mobiata.android.util.SettingUtils

class FeatureToggleUtil {
    companion object {
        @JvmStatic fun isUserBucketedAndFeatureEnabled(context: Context, abacusTestKey: Int, featureKey: Int, featureDefaultValue: Boolean): Boolean {
            val isTestBucketed = isTestBucketed(abacusTestKey)
            val isFeatureEnabled = isFeatureEnabled(context, featureKey, featureDefaultValue)
            return isTestBucketed && isFeatureEnabled
        }

        @JvmStatic fun isFeatureEnabled(context: Context, featureKey: Int, featureDefaultValue: Boolean): Boolean {
            val isFeatureEnabled = SettingUtils.get(context, featureKey, featureDefaultValue)
            return isFeatureEnabled
        }

        @JvmStatic fun isTestBucketed(abacusTestKey: Int): Boolean {
            val isTestBucketed = Db.getAbacusResponse().isUserBucketedForTest(abacusTestKey)
            return isTestBucketed
        }

    }
}

