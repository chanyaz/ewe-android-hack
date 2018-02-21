package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinHotelParsingTest {

    @Test
    fun testHotelParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("fb24d134-adbd-44f6-9904-48cfb33bbd50", itin?.tripId)
        assertEquals("https://www.expedia.com/trips/1103274148635", itin?.webDetailsURL)
        assertEquals("1103274148635", itin?.tripNumber)
        assertEquals("Mobile Itinerary", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val hotel = itin?.hotels?.first()
        assertNotNull(hotel)
        assertEquals("5D5D96B6-0BB4-4A08-B82F-C9FBADF85532_0", hotel?.uniqueID)
    }
}
