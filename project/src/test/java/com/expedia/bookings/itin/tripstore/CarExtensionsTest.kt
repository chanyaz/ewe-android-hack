package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import org.junit.Test
import kotlin.test.assertEquals

class CarExtensionsTest {
    @Test
    fun happyPickUp() {
        val happyItin = ItinMocker.carDetailsHappy.cars?.first()

        assertEquals("Sydney, Victoria, AUS, 98188", happyItin?.pickupLocation?.buildSecondaryAddress())
        assertEquals("Sir John Young Crescent Domain Car Park, Sydney, Victoria, AUS, 98188", happyItin?.pickupLocation?.buildFullAddress())
    }

    @Test
    fun happyDropOff() {
        val happyItin = ItinMocker.carDetailsHappy.cars?.first()

        assertEquals("Docklands, Victoria, AUS, 98188", happyItin?.dropOffLocation?.buildSecondaryAddress())
        assertEquals("99 Spencer Street, Docklands, Victoria, AUS, 98188", happyItin?.dropOffLocation?.buildFullAddress())
    }

    @Test
    fun missingPickUpTest() {
        val happyItin = ItinMocker.carDetailsBadLocations.cars?.first()

        assertEquals("AUS", happyItin?.pickupLocation?.buildSecondaryAddress())
        assertEquals("AUS", happyItin?.pickupLocation?.buildFullAddress())
    }

    @Test
    fun missingDropOffTest() {
        val happyItin = ItinMocker.carDetailsBadLocations.cars?.first()

        assertEquals("Docklands, Victoria", happyItin?.dropOffLocation?.buildSecondaryAddress())
        assertEquals("Docklands, Victoria", happyItin?.dropOffLocation?.buildFullAddress())
    }
}
