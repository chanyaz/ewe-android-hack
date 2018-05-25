package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.buildDropOffSecondaryAddress
import com.expedia.bookings.itin.tripstore.extensions.buildFullDropOffAddress
import com.expedia.bookings.itin.tripstore.extensions.buildFullPickupAddress
import com.expedia.bookings.itin.tripstore.extensions.buildPickupSecondaryAddress
import org.junit.Test
import kotlin.test.assertEquals

class CarExtensionsTest {
    @Test
fun happyPickUp() {
        val happyItin = ItinMocker.carDetailshappy.cars?.first()

        assertEquals("Sydney, Victoria, AUS, 98188", happyItin?.buildPickupSecondaryAddress())
        assertEquals("Sir John Young Crescent Domain Car Park, Sydney, Victoria, AUS, 98188", happyItin?.buildFullPickupAddress())
    }

    @Test
    fun happyDropOff() {
        val happyItin = ItinMocker.carDetailshappy.cars?.first()

        assertEquals("Docklands, Victoria, AUS, 98188", happyItin?.buildDropOffSecondaryAddress())
        assertEquals("99 Spencer Street, Docklands, Victoria, AUS, 98188", happyItin?.buildFullDropOffAddress())
    }

    @Test
    fun missingPickUpTest() {
        val happyItin = ItinMocker.carDetailsBadLocations.cars?.first()

        assertEquals("AUS", happyItin?.buildPickupSecondaryAddress())
        assertEquals("Sir John Young Crescent Domain Car Park, AUS", happyItin?.buildFullPickupAddress())
    }

    @Test
    fun missingDropOffTest() {
        val happyItin = ItinMocker.carDetailsBadLocations.cars?.first()

        assertEquals("Docklands, Victoria", happyItin?.buildDropOffSecondaryAddress())
        assertEquals("Docklands, Victoria", happyItin?.buildFullDropOffAddress())
    }
}