package com.expedia.bookings.test

import java.math.BigDecimal
import java.util.ArrayList

import kotlin.test.assertEquals

import org.junit.Test

import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.Money
import com.expedia.bookings.tracking.FacebookEvents

class FacebookEventsTest {

    @Test
    fun testCalculateLowestRateFlightsWithThreeFlights() {
        val flightTrips = ArrayList<FlightTrip>()
        flightTrips.add(initFlightTripWithPrice(BigDecimal("3.15")))
        flightTrips.add(initFlightTripWithPrice(BigDecimal("3.14")))
        flightTrips.add(initFlightTripWithPrice(BigDecimal("7.34")))
        assertEquals("3.14", FacebookEvents().calculateLowestRateFlights(flightTrips))
    }

    @Test
    fun testCalculateLowestRateFlightsZeroLengthInput() {
        val emptyFlightTrips = ArrayList<FlightTrip>()
        assertEquals("", FacebookEvents().calculateLowestRateFlights(emptyFlightTrips))
    }

    // Helper method that makes a blank FlightTrip with the given fare.
    fun initFlightTripWithPrice(fare: BigDecimal): FlightTrip {
        val flightTrip = FlightTrip()
        flightTrip.totalPrice = Money(fare, "USD")
        return flightTrip
    }
}
