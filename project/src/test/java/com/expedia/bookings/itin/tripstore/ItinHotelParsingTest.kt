package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.HotelRoom
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItinHotelParsingTest {
    @Test
    fun testItinParsing() {
        val itin = getMockItin()

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
    }

    @Test
    fun testHotelParsing() {
        val hotel = getMockHotel()

        assertNotNull(hotel)
        assertEquals("5D5D96B6-0BB4-4A08-B82F-C9FBADF85532_0", hotel?.uniqueID)
    }

    @Test
    fun testHotelRoomParsing() {
        val rooms = getMockHotelRooms()

        assertNotNull(rooms)
        assertEquals(2, rooms?.size)

        if (rooms != null) {
            for (room in rooms) {
                assertNotNull(room.totalPriceDetails)
            }
        }
    }

    @Test
    fun testHotelRoomTotalPriceDetailsParsing() {
        val room = getMockHotelRooms()?.first()

        assertNotNull(room)

        if (room != null) {
            val totalPriceDetails = room.totalPriceDetails
            val priceDetailsPerDay = totalPriceDetails?.priceDetailsPerDay

            assertEquals("$430.80", totalPriceDetails?.totalFormatted)
            assertNotNull(priceDetailsPerDay)
            assertEquals(1, priceDetailsPerDay?.size)
        }
    }

    @Test
    fun testPriceDetailsPerDayParsing() {
        val priceDetail = getMockHotelRooms()?.firstOrNull()?.totalPriceDetails?.priceDetailsPerDay?.firstOrNull()

        assertNotNull(priceDetail)

        if (priceDetail != null) {
            assertEquals("$293.57", priceDetail.amountFormatted)
            assertEquals("Sun, May 20", priceDetail.localizedDay?.localizedFullDate)
        }
    }

    private fun createMockJson(): ItinDetailsResponse? {
        return mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")
    }

    private fun getMockItin(): Itin? {
        return createMockJson()?.itin
    }

    private fun getMockHotel(): ItinHotel? {
        return getMockItin()?.firstHotel()
    }

    private fun getMockHotelRooms(): List<HotelRoom>? {
        return getMockHotel()?.rooms
    }
}
