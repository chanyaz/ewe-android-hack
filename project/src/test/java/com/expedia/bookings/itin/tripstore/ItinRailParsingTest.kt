package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItinRailParsingTest {

    @Test
    fun railParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/rail_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("efb7b989-25fa-4753-bf03-a8af9fcd4b41", itin?.tripId)
        assertEquals("https://wwwexpediade.integration.sb.karmalab.net/trips/79004578809", itin?.webDetailsURL)
        assertEquals("79004578809", itin?.tripNumber)
        assertEquals("Berlin Hauptbahnhof-Frankfurt (Main) Hauptbahnhof", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val rail = itin?.rails?.first()
        assertNotNull(rail)
        assertNull(rail?.uniqueID)
    }
}
