package com.expedia.bookings.utils


import com.expedia.util.SatellitePref
import org.junit.Test
import kotlin.test.assertEquals

class SatelliteTimestampTest {

    @Test
    fun testTimestampValidity() {
        var call = SatellitePref().timestampCheck(1501796173,1501882575,86400)
        var expectedResponse  = true
        assertEquals(expectedResponse, call)
        call = SatellitePref().timestampCheck(1501796173,1501800000,86400)
        expectedResponse  = false
        assertEquals(expectedResponse, call)
    }
}