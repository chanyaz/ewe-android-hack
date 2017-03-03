package com.expedia.bookings.test.robolectric

import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import kotlin.test.assertEquals

object RoboTestHelper {
    fun updateABTest(key: Int, value: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(key, value)
        Db.setAbacusResponse(abacusResponse)
    }

    fun bucketTests(vararg tests: Int) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }

    fun controlTests(vararg tests: Int) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }

    fun assertVisible(view: View) {
        assertEquals(View.VISIBLE, view.visibility)
    }
    
}