package com.expedia.bookings.data

import com.expedia.bookings.data.trips.ItinFlightLegTime
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightLegTest {
    lateinit var sut: FlightLeg

    @Before
    fun setup() {
        sut = FlightLeg()
    }

    @Test
    fun testToJsonForLegTime() {
        sut.legDepartureTime = ItinFlightLegTime()
        sut.legArrivalTime = ItinFlightLegTime()
        val json = sut.toJson()
        assertTrue(json.has("legDepartureTime"))
        assertTrue(json.has("legArrivaltime"))
    }

    @Test
    fun testToJsonNoLegTime() {
        val json = sut.toJson()
        assertFalse(json.has("legDepartureTime"))
        assertFalse(json.has("legArrivaltime"))
    }

    @Test
    fun testFromJsonForLegTime() {
        val card = ItinCardDataFlightBuilder().build()
        val json = card.flightLeg.toJson()
        val testFlightLeg = FlightLeg()
        testFlightLeg.fromJson(json)
        assertNotNull(testFlightLeg.legArrivalTime)
        assertNotNull(testFlightLeg.legDepartureTime)
    }

    @Test
    fun testFromJsonForNoLegTime() {
        val card = ItinCardDataFlightBuilder().build()
        val leg = card.flightLeg
        leg.legArrivalTime = null
        leg.legDepartureTime = null
        val json = leg.toJson()
        val testFlightLeg = FlightLeg()
        testFlightLeg.fromJson(json)
        assertNull(testFlightLeg.legArrivalTime)
        assertNull(testFlightLeg.legDepartureTime)
    }
}