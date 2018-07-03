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

class LxExtensionsTest {
    @Test
    fun buildSecondaryAddressHappyTest() {
        val happyItin = ItinMocker.lxDetailsHappy.activities?.first()
        assertEquals("San Francisco, CA, USA, 94118", happyItin?.buildSecondaryAddress())
    }

    @Test
    fun buildSecondaryAddressNoCountryTest() {
        val badItin = ItinMocker.lxDetailsNoTripID.activities?.first()
        assertEquals("San Francisco, CA, 94118", badItin?.buildSecondaryAddress())
    }

    @Test
    fun buildFullAddressHappyTest() {
        val happyItin = ItinMocker.lxDetailsHappy.activities?.first()
        assertEquals("55 Music Concourse Drive, San Francisco, CA, USA, 94118", happyItin?.buildFullAddress())
    }

    @Test
    fun buildFullAddressNoCountryTest() {
        val happyItin = ItinMocker.lxDetailsNoTripID.activities?.first()
        assertEquals("55 Music Concourse Drive, San Francisco, CA, 94118", happyItin?.buildFullAddress())
    }

    @Test
    fun getLatLongHappyPathTest() {
        val happyItin = ItinMocker.lxDetailsHappy.activities?.first()
        assertEquals(37.76974, happyItin?.redemptionLocations?.firstOrNull()?.latitude)
        assertEquals(-122.46614, happyItin?.redemptionLocations?.firstOrNull()?.longitude)
        assertNotNull(happyItin?.getLatLng())
    }

    @Test
    fun getLatLongOnlyLongReturnedTest() {
        val itinWithNoLat = ItinMocker.lxDetailsNoDetailsUrl.activities?.first()
        assertNull(itinWithNoLat?.redemptionLocations?.firstOrNull()?.latitude)
        assertEquals(-122.46614, itinWithNoLat?.redemptionLocations?.firstOrNull()?.longitude)
        assertNull(itinWithNoLat?.getLatLng())
    }

    @Test
    fun getLatLongOnlyLatReturnedTest() {
        val itinWithNoLng = ItinMocker.lxDetailsInvalidLatLong.activities?.first()
        assertEquals(37.76974, itinWithNoLng?.redemptionLocations?.firstOrNull()?.latitude)
        assertNull(itinWithNoLng?.redemptionLocations?.firstOrNull()?.longitude)
        assertNull(itinWithNoLng?.getLatLng())
    }

    @Test
    fun getLatLongWhenBothAreNullTest() {
        val itinWithNoLatLng = ItinMocker.lxDetailsNoLatLong.activities?.first()
        assertNull(itinWithNoLatLng?.redemptionLocations?.firstOrNull()?.latitude)
        assertNull(itinWithNoLatLng?.redemptionLocations?.firstOrNull()?.longitude)
        assertNull(itinWithNoLatLng?.getLatLng())
    }

    @Test
    fun getNameLocationPairHappyPathTest() {
        val happyItin = ItinMocker.lxDetailsHappy.activities?.first()
        assertEquals("California Academy of Sciences", happyItin?.redemptionLocations?.firstOrNull()?.name1)
        assertEquals("San Francisco, CA, USA, 94118", happyItin?.buildSecondaryAddress())
        assertEquals(happyItin?.getNameLocationPair(), Pair("California Academy of Sciences", "San Francisco, CA, USA, 94118"))
    }

    @Test
    fun getNameLocationPairNameNotReturnedTest() {
        val happyItin = ItinMocker.lxDetailsNoLat.activities?.first()
        assertNull(happyItin?.redemptionLocations?.firstOrNull()?.name1)
        assertEquals("San Francisco, CA, USA, 94118", happyItin?.buildSecondaryAddress())
        assertEquals(happyItin?.getNameLocationPair(), Pair(null, "San Francisco, CA, USA, 94118"))
    }

    @Test
    fun getNameLocationPairLocationNotReturnedTest() {
        val happyItin = ItinMocker.lxDetailsNoDetailsUrl.activities?.first()
        assertEquals("California Academy of Sciences", happyItin?.redemptionLocations?.firstOrNull()?.name1)
        assertTrue(happyItin?.buildSecondaryAddress().isNullOrEmpty())
        assertEquals(happyItin?.getNameLocationPair(), Pair("California Academy of Sciences", ""))
    }
}
