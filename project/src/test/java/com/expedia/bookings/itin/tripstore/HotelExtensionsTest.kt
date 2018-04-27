package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.isPointOfSaleDifferentFromPointOfSupply
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelExtensionsTest {

    @Test
    fun testIsPointOfSaleDifferentFromPointOfSupplyTrue() {
        val hotel = ItinMocker.hotelDetailsHappy.firstHotel()!!
        assertTrue(hotel.isPointOfSaleDifferentFromPointOfSupply())
    }

    @Test
    fun testIsPointOfSaleDifferentFromPointOfSupplyFalse() {
        val hotel1 = ItinMocker.hotelDetailsPosSameAsPoSu.firstHotel()!!
        assertFalse(hotel1.isPointOfSaleDifferentFromPointOfSupply())

        val hotel2 = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()!!
        assertFalse(hotel2.isPointOfSaleDifferentFromPointOfSupply())
    }
}
