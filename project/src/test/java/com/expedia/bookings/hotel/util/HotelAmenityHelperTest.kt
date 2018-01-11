package com.expedia.bookings.hotel.util

import com.expedia.bookings.hotel.data.HotelAmenity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelAmenityHelperTest {
    @Test
    fun testFilterOrder() {
        val filterAmenities = HotelAmenityHelper.getFilterAmenities()

        assertEquals(HotelAmenity.BREAKFAST, filterAmenities[0], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.POOL, filterAmenities[1], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.PARKING, filterAmenities[2], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.PETS, filterAmenities[3], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.INTERNET, filterAmenities[4], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.AIRPORT_SHUTTLE, filterAmenities[5], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.AC_UNIT, filterAmenities[6], "FAILURE: Order Matters")
        assertEquals(HotelAmenity.ALL_INCLUSIVE, filterAmenities[7], "FAILURE: Order Matters")
    }

    @Test
    fun testSearchKeys() {
        assertEquals("16", HotelAmenityHelper.getSearchKey(HotelAmenity.BREAKFAST))
        assertEquals("7", HotelAmenityHelper.getSearchKey(HotelAmenity.POOL))
        assertEquals("14", HotelAmenityHelper.getSearchKey(HotelAmenity.PARKING))
        assertEquals("19", HotelAmenityHelper.getSearchKey(HotelAmenity.INTERNET))
        assertEquals("17", HotelAmenityHelper.getSearchKey(HotelAmenity.PETS))
        assertEquals("66", HotelAmenityHelper.getSearchKey(HotelAmenity.AIRPORT_SHUTTLE))
        assertEquals("27", HotelAmenityHelper.getSearchKey(HotelAmenity.AC_UNIT))
        assertEquals("30", HotelAmenityHelper.getSearchKey(HotelAmenity.ALL_INCLUSIVE))
        assertEquals("", HotelAmenityHelper.getSearchKey(HotelAmenity.KITCHEN))
    }
}
