package com.expedia.bookings.test

import com.expedia.bookings.tracking.OmnitureTracking
import org.junit.Test
import kotlin.test.assertEquals

class OmnitureProductStringTest {

    @Test
    fun testHotelProductString() {
        val actualProductString = OmnitureTracking.getHotelProductString("122343", 2, "320.23", "DirectAgency")
        val expectedProductString = "Hotel;DirectAgency Hotel:122343;2;320.23"
        assertEquals(expectedProductString, actualProductString)

        val actualProductStringNoSupplier = OmnitureTracking.getHotelProductString("122343", 2, "320.23", "")
        val expectedProductStringNoSupplier = "Hotel; Hotel:122343;2;320.23"
        assertEquals(expectedProductStringNoSupplier, actualProductStringNoSupplier)

        val actualProductStringNoTotalCost = OmnitureTracking.getHotelProductString("122343", 2, null, "DirectAgency")
        val expectedProductStringNoTotalCost = "Hotel;DirectAgency Hotel:122343;2;"
        assertEquals(expectedProductStringNoTotalCost, actualProductStringNoTotalCost)

        val actualProductStringNoHotelId = OmnitureTracking.getHotelProductString(null, 2, "320.23", "DirectAgency")
        val expectedProductStringNoHotelId = "Hotel;DirectAgency Hotel:;2;320.23"
        assertEquals(expectedProductStringNoHotelId, actualProductStringNoHotelId)
    }
}
