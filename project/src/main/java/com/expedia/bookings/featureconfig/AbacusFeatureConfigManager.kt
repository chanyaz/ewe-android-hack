package com.expedia.bookings.featureconfig

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.util.ForceBucketPref
import com.mobiata.android.util.SettingUtils

class AbacusFeatureConfigManager {

    companion object {
        @Deprecated("Use isUserBucketedForTest(context, ABTest) instead")
        @JvmStatic
        fun isUserBucketedForTest(abacusTest: ABTest): Boolean {
            return isInVariant(abacusTest, AbacusVariant.BUCKETED)
        }

        @JvmStatic
        fun isUserBucketedForTest(context: Context, abacusTest: ABTest): Boolean {
            return isBucketedForVariant(context, abacusTest, AbacusVariant.BUCKETED)
        }

        /**
         * Determines if a test is in bucketed into any test variant via abacus and remotely
         * via satellite (if applicable).  This function is helpful when you have functionality
         * that is available to all variants of a multi-variant test. Common for variant tests that
         * are just simple text changes.
         */
        @JvmStatic
        fun isBucketedInAnyVariant(context: Context, abacusTest: ABTest): Boolean {
            return isBucketedForVariant(context, abacusTest, AbacusVariant.BUCKETED)
                    || isBucketedForVariant(context, abacusTest, AbacusVariant.ONE)
                    || isBucketedForVariant(context, abacusTest, AbacusVariant.TWO)
                    || isBucketedForVariant(context, abacusTest, AbacusVariant.THREE)
        }

        @JvmStatic
        fun shouldTrackTest(context: Context, abacusTest: ABTest): Boolean {
            if (abacusTest.remote && !SatelliteFeatureConfigManager.isABTestEnabled(context, abacusTest.key) && !useOverride(context, abacusTest)) {
                return false
            }
            return true
        }

        fun isBucketedForVariant(context: Context, abacusTest: ABTest,
                                 variant: AbacusVariant): Boolean {
            if (abacusTest.remote && !(useOverride(context, abacusTest))) {
                return isUserInVariantForRemoteTest(context, abacusTest, variant)
            }
            return isInVariant(abacusTest, variant)
        }

        private fun isUserInVariantForRemoteTest(context: Context, abacusTest: ABTest,
                                                 variant: AbacusVariant): Boolean {
            if (SatelliteFeatureConfigManager.isABTestEnabled(context, abacusTest.key)) {
                return isInVariant(abacusTest, variant)
            }
            return false
        }

        private fun isInVariant(abacusTest: ABTest, variant: AbacusVariant): Boolean {
            return variant.value == Db.sharedInstance.abacusResponse.variateForTest(abacusTest)
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
            return BuildConfig.DEBUG && SettingUtils.get(context, abacusTest.key.toString(), AbacusVariant.DEBUG.value) != AbacusVariant.DEBUG.value
        }

        private fun isForceBucketed(context: Context, abacusTest: ABTest): Boolean {
            return ForceBucketPref.isForceBucketed(context)
                    && ForceBucketPref.getForceBucketedTestValue(context, abacusTest.key,
                    AbacusVariant.DEBUG.value) != AbacusVariant.DEBUG.value
        }
    }
}
