package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelRoomResponseTest {
    lateinit var roomResponse: HotelOffersResponse.HotelRoomResponse

    @Before
    fun setUp() {
        roomResponse = HotelOffersResponse.HotelRoomResponse()
    }

    @Test
    fun testIsMerchant() {
        assertFalse(roomResponse.isMerchant)
        roomResponse.supplierType = ""
        assertFalse(roomResponse.isMerchant)
        roomResponse.supplierType = "RANDOM"
        assertFalse(roomResponse.isMerchant)
        roomResponse.supplierType = "E"
        assertTrue(roomResponse.isMerchant)
        roomResponse.supplierType = "MERCHANT"
        assertTrue(roomResponse.isMerchant)
        roomResponse.supplierType = "NOTANYMORE"
        assertFalse(roomResponse.isMerchant)
    }

    @Test
    fun testFormattedBedNames() {
        assertEquals("", roomResponse.formattedBedNames)
        val beds = ArrayList<HotelOffersResponse.BedTypes>()
        val bed1 = HotelOffersResponse.BedTypes()
        bed1.id = "1"
        bed1.description = "1"
        beds.add(bed1)
        roomResponse.bedTypes = beds
        assertEquals("1", roomResponse.formattedBedNames)

        val bed2 = HotelOffersResponse.BedTypes()
        bed2.id = "2"
        bed2.description = "2"
        beds.add(bed2)
        roomResponse.bedTypes = beds
        assertEquals("1, 2", roomResponse.formattedBedNames)

        beds.add(bed1)
        assertEquals("1, 2, 1", roomResponse.formattedBedNames)
    }

    @Test
    fun testIsPackage() {
        assertFalse(roomResponse.isPackage)
        roomResponse.packageHotelDeltaPrice = Money()
        assertTrue(roomResponse.isPackage)
    }

    @Test
    fun testRoomTypeDescriptionWithoutDetail() {
        roomResponse.roomTypeDescription = "Room Type"
        assertEquals("Room Type", roomResponse.roomTypeDescriptionWithoutDetail)

        roomResponse.roomTypeDescription = "Room Type - some details"
        assertEquals("Room Type", roomResponse.roomTypeDescriptionWithoutDetail)

        roomResponse.roomTypeDescription = "Room Type-some details"
        assertEquals("Room Type-some details", roomResponse.roomTypeDescriptionWithoutDetail)
    }

    @Test
    fun testRoomTypeDescriptionDetail() {
        roomResponse.roomTypeDescription = "Room Type"
        assertEquals("", roomResponse.roomTypeDescriptionDetail)

        roomResponse.roomTypeDescription = "Room Type - some details"
        assertEquals("some details", roomResponse.roomTypeDescriptionDetail)

        roomResponse.roomTypeDescription = "Room Type-some details"
        assertEquals("", roomResponse.roomTypeDescriptionDetail)
    }
}
