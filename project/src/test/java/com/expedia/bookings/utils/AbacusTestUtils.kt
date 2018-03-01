package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusVariant
import com.mobiata.android.util.SettingUtils

object AbacusTestUtils {

    @JvmStatic fun resetABTests() {
        Db.sharedInstance.abacusResponse = AbacusResponse()
    }

    @JvmStatic fun updateABTest(test: ABTest, value: Int) {
        val abacusResponse = Db.sharedInstance.abacusResponse
        abacusResponse.updateABTestForDebug(test.key, value)
    }

    @JvmStatic fun bucketTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusVariant.BUCKETED.value)
        }
        Db.sharedInstance.abacusResponse = abacusResponse
    }

    @JvmStatic fun bucketTestAndEnableRemoteFeature(context: Context, test: ABTest, bucketVariant: Int = AbacusVariant.BUCKETED.value) {
        SettingUtils.save(context, test.key.toString(), bucketVariant)
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(test.key, bucketVariant)
        Db.sharedInstance.abacusResponse = abacusResponse
    }

    @JvmStatic
    fun bucketTestsAndEnableRemoteFeature(context: Context, vararg tests: ABTest) {
        tests.forEach {
            SettingUtils.save(context, it.key.toString(), AbacusVariant.BUCKETED.value)
        }
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusVariant.BUCKETED.value)
        }
        Db.sharedInstance.abacusResponse = abacusResponse
    }

    @JvmStatic fun bucketTestWithVariant(test: ABTest, variant: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(test.key, variant)
        Db.sharedInstance.abacusResponse = abacusResponse
    }

    @JvmStatic fun unbucketTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusVariant.CONTROL.value)
        }
        Db.sharedInstance.abacusResponse = abacusResponse
    }
}
