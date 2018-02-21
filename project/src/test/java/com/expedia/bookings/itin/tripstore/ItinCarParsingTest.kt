package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinCarParsingTest {

    @Test
    fun carParsing() {
        val json = mockObject(ItinDetailsResponse::class.java, "api/trips/car_trip_details.json")
        val itin = json?.itin

        assertNotNull(itin)
        assertEquals("a81951a6-c740-4db9-9bcd-2862076fff1b", itin?.tripId)
        assertEquals("https://www.expedia.com/trips/7283429594044?email=", itin?.webDetailsURL)
        assertEquals("7283429594044", itin?.tripNumber)
        assertEquals("Car rental in Hebron", itin?.title)
        assertEquals("BOOKED", itin?.bookingStatus)

        val startTime = itin?.startTime
        val endTime = itin?.endTime
        assertEquals(startTime, itin?.startTime)
        assertEquals(endTime, itin?.endTime)

        val car = itin?.cars?.first()
        assertNotNull(car)
        assertEquals("426C7990-C60E-433E-852C-80886A594FCB_0", car?.uniqueID)
    }
}
