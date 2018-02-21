package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TripExtensionsTest {

    @Test
    fun testFirstHotelValidHotel() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details.json")
        assertNotNull(hotel)
        assertEquals("5D5D96B6-0BB4-4A08-B82F-C9FBADF85532_0", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelPackageHotel() {
        val hotel = makeItinHotel("api/trips/package_trip_details.json")
        assertNotNull(hotel)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelArrayEmpty() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_no_hotels.json")
        assertNull(hotel)
    }

    @Test
    fun testFirstHotelNoHotels() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_empty_hotels.json")
        assertNull(hotel)
    }

    private fun makeItinHotel(mockName: String): ItinHotel? {
        val json = mockObject(ItinDetailsResponse::class.java, mockName)
        val itin = json?.itin
        return itin?.firstHotel()
    }
}
