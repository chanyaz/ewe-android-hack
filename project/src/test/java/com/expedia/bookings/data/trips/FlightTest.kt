package com.expedia.bookings.data.trips

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Seat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals


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
}
