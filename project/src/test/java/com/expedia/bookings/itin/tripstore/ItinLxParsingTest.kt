package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinLxParsingTest {

    @Test
    fun lxParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/activity_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("b9739936-62a8-49a1-af12-fdbe85d78e5f", itin?.tripId)
        assertEquals("https://wwwexpediacom.trunk.sb.karmalab.net/trips/71196729802", itin?.webDetailsURL)
        assertEquals("71196729802", itin?.tripNumber)
        assertEquals("New York, United States", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val lx = itin?.activities?.first()
        assertNotNull(lx)
        assertEquals("200E974C-C7DA-445E-A392-DD12578A96A0_0_358734_358736", lx?.uniqueID)
        assertEquals("San Francisco", lx?.activityLocation?.city)
    }
}
