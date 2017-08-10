package com.expedia.bookings.utils

import android.text.format.DateUtils
import com.expedia.util.SatelliteConfigManager
import org.junit.Test
import kotlin.test.assertEquals

class SatelliteTimestampTest {
    val currentTime = System.currentTimeMillis()
    val oneDay = DateUtils.DAY_IN_MILLIS

    @Test
    fun testTimestampValid() {
        val call = SatelliteConfigManager().isTimeStampValid(currentTime, currentTime + oneDay - 100, oneDay)
        val expectedResponse = false
        assertEquals(expectedResponse, call)
    }

    @Test
    fun testTimestampInvalid() {
        val call = SatelliteConfigManager().isTimeStampValid(currentTime, currentTime + oneDay + 100, oneDay)
        val expectedResponse = true
        assertEquals(expectedResponse, call)
    }
}
