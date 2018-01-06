package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.mobiata.android.util.SettingUtils

object AbacusTestUtils {

    @JvmStatic fun resetABTests() {
        Db.sharedInstance.setAbacusResponse(AbacusResponse())
    }

    @JvmStatic fun updateABTest(test: ABTest, value: Int) {
        val abacusResponse = Db.sharedInstance.abacusResponse
        abacusResponse.updateABTestForDebug(test.key, value)
    }

    @JvmStatic fun bucketTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        }
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun bucketTestAndEnableRemoteFeature(context: Context,  test: ABTest, bucketVariant: Int = AbacusUtils.DefaultVariant.BUCKETED.ordinal) {
        SettingUtils.save(context, test.key.toString(), bucketVariant)
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(test.key, bucketVariant)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun bucketTestAndEnableFeature(context: Context, abacusTest: ABTest, @StringRes featureKey: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(abacusTest.key, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
        SettingUtils.save(context, featureKey, true)
    }

    @JvmStatic fun bucketTestWithVariant(test: ABTest, variant: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(test.key, variant)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun unbucketTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        }
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun unbucketTestAndDisableFeature(context: Context, abacusTest: ABTest, @StringRes featureKey: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(abacusTest.key, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
        SettingUtils.save(context, featureKey, false)
    }
}
