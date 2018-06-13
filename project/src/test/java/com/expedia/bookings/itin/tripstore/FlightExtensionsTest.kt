package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.getLegs
import org.junit.Test
import kotlin.test.assertEquals

class FlightExtensionsTest {
    @Test
    fun getLegsHappy() {
        val itin = ItinMocker.flightDetailsHappy
        val legsSubject = itin.getLegs()
        assertEquals(1, legsSubject.size)
        assertEquals(listOf(itin.flights?.get(0)?.legs?.get(0)), legsSubject)
    }

    @Test
    fun getLegsMultiSegment() {
        val itin = ItinMocker.flightDetailsHappyMultiSegment
        val legsSubject = itin.getLegs()
        assertEquals(2, legsSubject.size)
        assertEquals(listOf(itin.flights?.get(0)?.legs?.get(0), itin.flights?.get(0)?.legs?.get(1)), legsSubject)
    }

    @Test
    fun getLegsPackage() {
        val itin = ItinMocker.hotelPackageHappy
        val legsSubject = itin.getLegs()
        assertEquals(2, legsSubject.size)
        assertEquals(listOf(itin.packages?.get(0)?.flights?.get(0)?.legs?.get(0), itin.packages?.get(0)?.flights?.get(0)?.legs?.get(1)), legsSubject)
    }

    @Test
    fun getLegsNonFlightItin() {
        val itin = ItinMocker.carDetailsHappy
        val legsSubject = itin.getLegs()
        assertEquals(0, legsSubject.size)
        assertEquals(emptyList(), legsSubject)
    }

    @Test
    fun getLegsSplitTicket() {
        val itin = ItinMocker.flightDetailsHappySplitTicket
        val legsSubject = itin.getLegs()
        assertEquals(2, legsSubject.size)
        assertEquals(listOf(itin.flights?.get(0)?.legs?.get(0), itin.flights?.get(1)?.legs?.get(0)), legsSubject)
    }
}
