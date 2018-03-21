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
        assertEquals("58fc868b-63e9-42cc-a0c3-6ac4dd78beaa", itin?.tripId)
        assertEquals("https://www.expedia.com/trips/7280999576135", itin?.webDetailsURL)
        assertEquals("7280999576135", itin?.tripNumber)
        assertEquals("Hotel in Bengaluru", itin?.title)
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
        assertEquals("B43C38B0-6F14-4D9D-BBEC-F56AEA3DF3B9_0", hotel?.uniqueID)
    }

    @Test
    fun testHotelRoomParsing() {
        val rooms = getMockHotelRooms()

        assertNotNull(rooms)
        assertEquals(1, rooms?.size)

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

            assertEquals("₹3,500.00", totalPriceDetails?.totalFormatted)
            assertNotNull(priceDetailsPerDay)
            assertEquals(4, priceDetailsPerDay?.size)
        }
    }

    @Test
    fun testPriceDetailsPerDayParsing() {
        val priceDetail = getMockHotelRooms()?.firstOrNull()?.totalPriceDetails?.priceDetailsPerDay?.firstOrNull()

        assertNotNull(priceDetail)

        if (priceDetail != null) {
            assertEquals("₹875.00", priceDetail.amountFormatted)
            assertEquals("Mon, Mar 12", priceDetail.localizedDay?.localizedFullDate)
        }
    }

    private fun createMockJson(): ItinDetailsResponse? {
        return mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")
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
