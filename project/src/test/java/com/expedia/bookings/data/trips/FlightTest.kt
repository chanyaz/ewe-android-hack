package com.expedia.bookings.data.trips

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Seat
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class FlightTest{

    lateinit var flight: Flight

    @Before
    fun setup() {
        flight = Flight()
    }

    @Test
    fun testGetFirstSixSeats() {
        val seats = "20A, 20B"
        assertEquals("20A, 20B", flight.getFirstSixSeats(seats))
    }

    @Test
    fun testGetFirstSixSeatsForTenSeats() {
        val seats = "20A, 20B, 20C, 21A, 21B, 21C, 22A, 22B, 22C, 22D"
        assertEquals("20A, 20B, 20C, 21A, 21B, 21C +4", flight.getFirstSixSeats(seats))
    }

    @Test
    fun testGetAssignedSeats() {
        val seatA = Seat("22A")
        val seatB = Seat("22B")
        flight.addSeat(seatA)
        flight.addSeat(seatB)
        assertEquals(flight.assignedSeats, "22A, 22B")
    }

    @Test
    fun testGetAssignedSeatsForNullSeats() {
        assertEquals(flight.seats.size, 0)
    }

    @Test
    fun testHasRedEyePlusOne() {
        val flightItinCard = ItinCardDataFlightBuilder().build()
        val flight = flightItinCard.flightLeg.segments[0]
        val depDate = flight.originWaypoint.bestSearchDateTime
        flight.destinationWaypoint = TestWayPoint(dateTime = depDate.plusDays(1))
        assertEquals(1, flight.daySpan())
        assertTrue(flight.hasRedEye())
    }
    @Test
    fun testHasRedEyeMinusOne() {
        val flightItinCard = ItinCardDataFlightBuilder().build()
        val flight = flightItinCard.flightLeg.segments[0]
        val depDate = flight.originWaypoint.bestSearchDateTime
        flight.destinationWaypoint = TestWayPoint(dateTime = depDate.minusDays(1))
        assertEquals(-1, flight.daySpan())
        assertTrue(flight.hasRedEye())
    }

    @Test
    fun testDoesNotHaveRedEye() {
        val flightItinCard = ItinCardDataFlightBuilder().build()
        val flight = flightItinCard.flightLeg.segments[0]
        val depDate = flight.originWaypoint.bestSearchDateTime
        flight.destinationWaypoint = TestWayPoint(dateTime = depDate)
        assertEquals(0, flight.daySpan())
        assertFalse(flight.hasRedEye())
    }

    class TestWayPoint(val dateTime: DateTime) : Waypoint(ACTION_UNKNOWN) {

        override fun getBestSearchDateTime(): DateTime = dateTime
    }
}
