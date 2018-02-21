package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinFlightParsingTest {

    @Test
    fun flightParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/flight_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("53a6459c-822c-4425-9e14-3eea43f38a97", itin?.tripId)
        assertEquals("https://www.expedia.com/trips/7238007847306", itin?.webDetailsURL)
        assertEquals("7238007847306", itin?.tripNumber)
        assertEquals("Flight to Las Vegas", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val flight = itin?.flights?.first()
        assertNotNull(flight)
        assertEquals("5B4874B5-3A4D-4B05-AC6A-81088EBAD678_0", flight?.uniqueID)
    }
}
