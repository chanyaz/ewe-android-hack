package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.mobiata.android.util.SettingUtils
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

object RoboTestHelper {
    fun updateABTest(abTest: ABTest, value: Int) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(abTest.key, value)
        Db.setAbacusResponse(abacusResponse)
    }

    fun bucketTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }

    fun controlTests(vararg tests: ABTest) {
        val abacusResponse = AbacusResponse()
        for (test in tests) {
            abacusResponse.updateABTestForDebug(test.key, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        }
        Db.setAbacusResponse(abacusResponse)
    }

    fun assertVisible(view: View) {
        assertEquals(View.VISIBLE, view.visibility)
    }

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
    }
}