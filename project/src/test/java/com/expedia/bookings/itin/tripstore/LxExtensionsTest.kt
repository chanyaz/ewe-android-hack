package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import org.junit.Test
import kotlin.test.assertEquals

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
}
