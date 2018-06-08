package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.expedia.bookings.itin.tripstore.extensions.getLatLng
import com.expedia.bookings.itin.tripstore.extensions.getNameLocationPair
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun getLatLngHappyPathTest() {
        val happyItin = ItinMocker.carDetailsHappy.cars?.first()

        assertEquals(-37.818294, happyItin?.dropOffLocation?.latitude)
        assertEquals(144.953432, happyItin?.dropOffLocation?.longitude)
        assertNotNull(happyItin?.getLatLng())
    }

    @Test
    fun getNameLocationPairHappyPathTest() {
        val happyItin = ItinMocker.carDetailsHappy.cars?.first()

        assertEquals("Thrifty", happyItin?.carVendor?.longName)
        assertEquals("Docklands, Victoria, AUS, 98188", happyItin?.dropOffLocation?.buildSecondaryAddress())
        assertNotNull(happyItin?.getNameLocationPair())
    }

    @Test
    fun getLatLngWhenLatIsNullTest() {
        val itinWithNoLat = ItinMocker.carDetailsBadNameAndImage.cars?.first()

        assertNull(itinWithNoLat?.dropOffLocation?.latitude)
        assertEquals(144.953432, itinWithNoLat?.dropOffLocation?.longitude)
        assertNull(itinWithNoLat?.getLatLng())
    }

    @Test
    fun getLatLngWhenLngIsNullTest() {
        val itinWithNoLng = ItinMocker.carDetailsBadPickupAndTimes.cars?.first()

        assertEquals(-37.818294, itinWithNoLng?.dropOffLocation?.latitude)
        assertNull(itinWithNoLng?.dropOffLocation?.longitude)
        assertNull(itinWithNoLng?.getLatLng())
    }

    @Test
    fun getNameLocationPairNameNotReturnedTest() {
        val itinWithNoName = ItinMocker.carDetailsBadNameAndImage.cars?.first()

        assertNull(itinWithNoName?.carVendor?.longName)
        assertEquals("Docklands, Victoria, AUS, 98188", itinWithNoName?.dropOffLocation?.buildSecondaryAddress())
        assertEquals(Pair(null, "Docklands, Victoria, AUS, 98188"), itinWithNoName?.getNameLocationPair())
    }

    @Test
    fun getNameLocationPairLocationNotReturnedTest() {
        val itinWithNoAddress = ItinMocker.carDetailsBadPickupAndTimes.cars?.first()

        assertEquals("Thrifty", itinWithNoAddress?.carVendor?.longName)
        assertTrue(itinWithNoAddress?.dropOffLocation?.buildSecondaryAddress().isNullOrEmpty())
        assertEquals(Pair("Thrifty", ""), itinWithNoAddress?.getNameLocationPair())
    }
}
