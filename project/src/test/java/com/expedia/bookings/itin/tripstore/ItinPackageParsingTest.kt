package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinPackageParsingTest {

    @Test
    fun packageParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/package_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("4d0385c3-9d0e-42ca-b7de-103d423f583c", itin?.tripId)
        assertEquals("https://www.expedia.com/trips/7313989476663", itin?.webDetailsURL)
        assertEquals("7313989476663", itin?.tripNumber)
        assertEquals("Trip to Barcelona", itin?.title)
        assertEquals("SAVED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val packageObject = itin?.packages?.first()
        assertNotNull(packageObject)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", packageObject?.uniqueID)

        val flight = itin?.packages?.first()
        assertNotNull(flight)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", flight?.uniqueID)

        val hotel = packageObject?.hotels?.first()
        assertNotNull(hotel)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", hotel?.uniqueID)
    }
}
