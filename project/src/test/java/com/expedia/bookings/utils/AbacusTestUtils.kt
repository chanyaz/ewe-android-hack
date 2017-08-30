package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.mobiata.android.util.SettingUtils

object AbacusTestUtils {

    @JvmStatic fun resetABTests() {
        Db.setAbacusResponse(AbacusResponse())
    }

    @JvmStatic fun updateABTest(key: Int, value: Int) {
        val abacusResponse = Db.getAbacusResponse()
        abacusResponse.updateABTestForDebug(key, value)
    }

    @JvmStatic fun bucketTests(vararg tests: Int) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun bucketTestAndEnableFeature(context: Context, abacusTestKey: Int, @StringRes featureKey: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(abacusTestKey, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        Db.setAbacusResponse(abacusResponse)
        SettingUtils.save(context, featureKey, true)
    }

    @JvmStatic fun bucketTestWithVariant(test: Int, variant: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(test, variant)
        Db.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun unbucketTests(vararg tests: Int) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }
}
