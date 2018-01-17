package com.expedia.bookings.hotel.data

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AmenityTest {
    @Test
    fun testFilterOrder() {
        val filterAmenities = Amenity.getFilterAmenities()

        assertEquals(Amenity.BREAKFAST, filterAmenities[0], "FAILURE: Order Matters")
        assertEquals(Amenity.POOL, filterAmenities[1], "FAILURE: Order Matters")
        assertEquals(Amenity.PARKING, filterAmenities[2], "FAILURE: Order Matters")
        assertEquals(Amenity.PETS, filterAmenities[3], "FAILURE: Order Matters")
        assertEquals(Amenity.INTERNET, filterAmenities[4], "FAILURE: Order Matters")
        assertEquals(Amenity.AIRPORT_SHUTTLE, filterAmenities[5], "FAILURE: Order Matters")
        assertEquals(Amenity.AC_UNIT, filterAmenities[6], "FAILURE: Order Matters")
        assertEquals(Amenity.ALL_INCLUSIVE, filterAmenities[7], "FAILURE: Order Matters")
    }

    @Test
    fun testSearchKeys() {
        assertEquals(16, Amenity.getSearchKey(Amenity.BREAKFAST))
        assertEquals(7, Amenity.getSearchKey(Amenity.POOL))
        assertEquals(14, Amenity.getSearchKey(Amenity.PARKING))
        assertEquals(19, Amenity.getSearchKey(Amenity.INTERNET))
        assertEquals(17, Amenity.getSearchKey(Amenity.PETS))
        assertEquals(66, Amenity.getSearchKey(Amenity.AIRPORT_SHUTTLE))
        assertEquals(27, Amenity.getSearchKey(Amenity.AC_UNIT))
        assertEquals(30, Amenity.getSearchKey(Amenity.ALL_INCLUSIVE))
        assertEquals(-1, Amenity.getSearchKey(Amenity.KITCHEN))
    }
}
