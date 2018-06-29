package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.getLatLng
import com.expedia.bookings.itin.tripstore.extensions.getNameLocationPair
import com.expedia.bookings.itin.tripstore.extensions.isPointOfSaleDifferentFromPointOfSupply
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

    @Test
    fun buildSecondaryAddressHappyTest() {
        val happyItin = ItinMocker.hotelDetailsHappy.hotels?.first()
        assertEquals("Bengaluru, Karnataka, IND, 560080", happyItin?.buildSecondaryAddress())
    }

    @Test
    fun buildSecondaryAddressNoCountryTest() {
        val badItin = ItinMocker.hotelDetailsPaidWithPointsFull.hotels?.first()
        assertEquals("Bangkok, THA, 10700", badItin?.buildSecondaryAddress())
    }

    @Test
    fun getLatLongHappyPathTest() {
        val happyItin = ItinMocker.hotelDetailsHappy.hotels?.first()
        assertEquals(13.014492, happyItin?.hotelPropertyInfo?.latitude)
        assertEquals(77.583052, happyItin?.hotelPropertyInfo?.longitude)
        assertNotNull(happyItin?.getLatLng())
    }

    @Test
    fun getLatLngOnlyLngReturnedTest() {
        val itinWithNoLat = ItinMocker.hotelDetailsNoPriceDetails.hotels?.first()
        assertNull(itinWithNoLat?.hotelPropertyInfo?.latitude)
        assertEquals(37.747423, itinWithNoLat?.hotelPropertyInfo?.longitude)
        assertNull(itinWithNoLat?.getLatLng())
    }

    @Test
    fun getLatLngOnlyLatReturnedTest() {
        val itinWithNoLng = ItinMocker.hotelDetailsExpediaCollect.hotels?.first()
        assertEquals(38.648842, itinWithNoLng?.hotelPropertyInfo?.latitude)
        assertNull(itinWithNoLng?.hotelPropertyInfo?.longitude)
        assertNull(itinWithNoLng?.getLatLng())
    }

    @Test
    fun getLatLngWhenBothAreNullTest() {
        val itinWithNoLatLng = ItinMocker.hotelDetailsPaidWithPointsFull.hotels?.first()
        assertNull(itinWithNoLatLng?.hotelPropertyInfo?.latitude)
        assertNull(itinWithNoLatLng?.hotelPropertyInfo?.longitude)
        assertNull(itinWithNoLatLng?.getLatLng())
    }

    @Test
    fun getNameLocationPairHappyPathTest() {
        val happyItin = ItinMocker.hotelDetailsHappy.hotels?.first()
        assertEquals("Crest Hotel", happyItin?.hotelPropertyInfo?.name)
        assertEquals("Bengaluru, Karnataka, IND, 560080", happyItin?.buildSecondaryAddress())
        assertEquals(happyItin?.getNameLocationPair(), Pair("Crest Hotel", "Bengaluru, Karnataka, IND, 560080"))
    }

    @Test
    fun getNameLocationPairNameNotReturnedTest() {
        val happyItin = ItinMocker.hotelDetailsNoPriceDetails.hotels?.first()
        assertNull(happyItin?.hotelPropertyInfo?.name)
        assertEquals("Moscow, RUS, 105613", happyItin?.buildSecondaryAddress())
        assertEquals(happyItin?.getNameLocationPair(), Pair(null, "Moscow, RUS, 105613"))
    }

    @Test
    fun getNameLocationPairLocationNotReturnedTest() {
        val happyItin = ItinMocker.hotelDetailsExpediaCollect.hotels?.first()
        assertEquals("Clayton Plaza Hotel", happyItin?.hotelPropertyInfo?.name)
        assertTrue(happyItin?.buildSecondaryAddress().isNullOrEmpty())
        assertEquals(happyItin?.getNameLocationPair(), Pair("Clayton Plaza Hotel", ""))
    }
}
