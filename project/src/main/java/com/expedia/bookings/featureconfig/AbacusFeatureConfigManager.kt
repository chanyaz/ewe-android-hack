package com.expedia.bookings.featureconfig

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.util.ForceBucketPref
import com.mobiata.android.util.SettingUtils

class AbacusFeatureConfigManager {

    companion object {
        @Deprecated("Use isUserBucketedForTest(context, ABTest) instead")
        @JvmStatic
        fun isUserBucketedForTest(abacusTest: ABTest): Boolean {
            return isBucketed(abacusTest)
        }

        @JvmStatic
        fun isUserBucketedForTest(context: Context, abacusTest: ABTest): Boolean {
            if (abacusTest.remote && !(useOverride(context, abacusTest))) {
                return isUserBucketedForRemoteTest(context, abacusTest)
            }
            return isBucketed(abacusTest)
        }

        @JvmStatic
        fun shouldTrackTest(context: Context, abacusTest: ABTest): Boolean {
            if (abacusTest.remote && !SatelliteFeatureConfigManager.isABTestEnabled(context, abacusTest.key) && !useOverride(context, abacusTest)) {
                return false
            }
            return true
        }

        private fun isUserBucketedForRemoteTest(context: Context, abacusTest: ABTest): Boolean {
            if (SatelliteFeatureConfigManager.isABTestEnabled(context, abacusTest.key)) {
                return isBucketed(abacusTest)
            }
            return false
        }

        private fun isBucketed(abacusTest: ABTest): Boolean {
            val test = Db.getAbacusResponse().testForKey(abacusTest)
            return test != null && test.isUserInBucket
        }

        private fun useOverride(context: Context, abacusTest: ABTest): Boolean {
            if (isForceBucketed(context, abacusTest)) {
                return true
            }

            if (isDebugOverride(context, abacusTest)) {
                return true
            }

            return false
        }

        private fun isDebugOverride(context: Context, abacusTest: ABTest): Boolean {
            return BuildConfig.DEBUG && SettingUtils.get(context, abacusTest.key.toString(), AbacusUtils.ABTEST_IGNORE_DEBUG) != AbacusUtils.ABTEST_IGNORE_DEBUG
        }

        private fun isForceBucketed(context: Context, abacusTest: ABTest): Boolean {
            return ForceBucketPref.isForceBucketed(context)
                    && ForceBucketPref.getForceBucketedTestValue(context, abacusTest.key,
                    AbacusUtils.ABTEST_IGNORE_DEBUG) != AbacusUtils.ABTEST_IGNORE_DEBUG
        }
    }
}