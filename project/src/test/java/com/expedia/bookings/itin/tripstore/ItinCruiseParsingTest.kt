package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItinCruiseParsingTest {

    @Test
    fun cruiseParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/cruise_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("8a246ebb-ef3d-43cc-aa9e-0bede99e38bd", itin?.tripId)
        assertEquals("https://wwwexpediacom.integration.sb.karmalab.net/trips/71296028520", itin?.webDetailsURL)
        assertEquals("71296028520", itin?.tripNumber)
        assertEquals("7-night Alaska Cruise from Seattle (Roundtrip)", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val cruise = itin?.cruises?.first()
        assertNotNull(cruise)
        assertNull(cruise?.uniqueID)
    }
}
