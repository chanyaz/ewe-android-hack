package com.expedia.bookings.test

import java.math.BigDecimal
import java.util.ArrayList

import kotlin.test.assertEquals

import org.junit.Test

import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.Money
import com.expedia.bookings.tracking.FacebookEvents

public class FacebookEventsTest {

    @Test
    fun testCalculateLowestRateFlightsWithThreeFlights() {
        var flightTrips = ArrayList<FlightTrip>()
        flightTrips.add(initFlightTripWithFare(BigDecimal("3.15")))
        flightTrips.add(initFlightTripWithFare(BigDecimal("3.14")))
        flightTrips.add(initFlightTripWithFare(BigDecimal("7.34")))
        assertEquals("3.14", FacebookEvents().calculateLowestRateFlights(flightTrips))
    }

    @Test
    fun testCalculateLowestRateFlightsZeroLengthInput() {
        var emptyFlightTrips = ArrayList<FlightTrip>()
        assertEquals("", FacebookEvents().calculateLowestRateFlights(emptyFlightTrips))
    }

    // Helper method that makes a blank FlightTrip with the given fare.
    fun initFlightTripWithFare(fare: BigDecimal): FlightTrip {
        var flightTrip = FlightTrip()
        flightTrip.totalFare = Money(fare, "USD")
        return flightTrip
    }
}
