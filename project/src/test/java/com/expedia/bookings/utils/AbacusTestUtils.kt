package com.expedia.bookings.utils

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils

object AbacusTestUtils {

    @JvmStatic fun updateABTest(key: Int, value: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(key, value)
        Db.setAbacusResponse(abacusResponse)
    }

    @JvmStatic fun bucketTests(vararg tests: Int) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
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
