package com.expedia.bookings.itin.vm

import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.helpers.ItinMocker
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class NewItinOmnitureUtilsTest {
    private lateinit var sut: ItinOmnitureUtils

    @Before
    fun setUp() {
        sut = ItinOmnitureUtils
    }

    @Test
    fun getNewHotelTripDuration() {
        val hotelItin = ItinMocker.hotelDetailsHappy
        val duration = sut.calculateTripDurationNew(hotelItin, ItinOmnitureUtils.LOB.HOTEL)
        assertEquals("4", duration)
    }

    @Test
    fun getNewLxTripDuration() {
        val lxItin = ItinMocker.lxDetailsHappy
        val duration = sut.calculateTripDurationNew(lxItin, ItinOmnitureUtils.LOB.LX)
        assertEquals("1", duration)
    }

    @Test
    fun getNewDaysUntilTrip() {
        val hotelItin = ItinMocker.hotelDetailsHappy
        val daysUntil = sut.calculateDaysUntilTripStartNew(hotelItin)
        assertEquals("0.0", daysUntil)
    }

    @Test
    fun buildNewOrderNumberAndItinNumberString() {
        val lxItin = ItinMocker.lxDetailsHappy
        val orderNumberAndItinNumberString = sut.buildOrderNumberAndItinNumberStringNew(lxItin)
        assertEquals("8104062917948|71196729802", orderNumberAndItinNumberString)
    }

    @Test
    fun buildNewHotelProductString() {
        val hotelItin = ItinMocker.hotelDetailsHappy
        val hotelProductString = sut.buildLOBProductString(hotelItin, ItinOmnitureUtils.LOB.HOTEL)
        assertEquals(";Hotel:17669432;4;10000.00", hotelProductString)
    }

    @Test
    fun buildNewLxProductString() {
        val lxItin = ItinMocker.lxDetailsAlsoHappy
        val lxProductString = sut.buildLOBProductString(lxItin, ItinOmnitureUtils.LOB.LX)
        assertEquals(";LX:219652;1;35.95", lxProductString)
    }
}
