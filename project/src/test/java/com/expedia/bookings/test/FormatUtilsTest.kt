package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.flightlib.data.Airline
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.FlightCode
import com.mobiata.flightlib.data.Waypoint
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils
import com.mobiata.flightlib.utils.FormatUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.TimeZone
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FormatUtilsTest {

    private fun getContext(): Context = RuntimeEnvironment.application

    @Test
    fun formatFlightNumberTest() {
        var flight: Flight? = null
        val unknown = getContext().getString(R.string.unknown)
        val noFlight = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(noFlight, unknown)

        flight = Flight()
        val noPrimaryFlightCode = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(noPrimaryFlightCode, unknown)

        flight.addFlightCode(FlightCode(), 0)
        val noAirLine = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(noAirLine, unknown + " ")

        val flightCode = FlightCode()
        flightCode.mAirlineCode = "TB"
        flight.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE)
        val airlineCode = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(flightCode.mAirlineCode + " ", airlineCode)

        flightCode.mNumber = "PRWVRU"
        flightCode.mAirlineCode = null
        flight.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE)
        val airlineNumber = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(unknown + " " + flightCode.mNumber, airlineNumber)

        flightCode.mAirlineCode = "UA"
        flight.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE)
        val airlineName = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals("United Airlines PRWVRU", airlineName)

        val airline = FlightStatsDbUtils.getAirline("DummyAirlineWithoutCode")
        flightCode.mAirlineCode = "DummyAirlineWithoutCode"
        airline.mAirlineCode = null
        flight.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE)
        val airlineWithoutNameAndCode = FormatUtils.formatFlightNumber(flight, getContext())
        assertEquals(unknown + " " + flightCode.mNumber, airlineWithoutNameAndCode)
    }

    @Test
    fun getCityNameTest() {
        val unknown = getContext().getString(R.string.unknown)
        val noWaypoint = FormatUtils.getCityName(null, getContext())
        assertEquals(unknown, noWaypoint)

        val waypoint = Waypoint(Waypoint.ACTION_ARRIVAL)
        val noAirport = FormatUtils.getCityName(waypoint, getContext())
        assertEquals(unknown, noAirport)

        waypoint.mAirportCode = "DummyCode"
        val airportCode = FormatUtils.getCityName(waypoint, getContext())
        assertEquals(waypoint.mAirportCode, airportCode)

        waypoint.mAirportCode = "SFO"
        val cityName = FormatUtils.getCityName(waypoint, getContext())
        assertEquals("San Francisco", cityName)

        val airport = waypoint.airport
        airport.mAirportCode = null
        airport.mCity = null
        val nocityAndCode = FormatUtils.getCityName(waypoint, getContext())
        assertEquals(unknown, nocityAndCode)
    }

    @Test
    fun formatAirlineTest() {
        val unknown = getContext().getString(R.string.unknown)
        val noAirline = FormatUtils.formatAirline(null, getContext())
        assertEquals(unknown, noAirline)

        val airline = Airline()
        val noNameAndCodeForAirline = FormatUtils.formatAirline(airline, getContext())
        assertEquals(unknown, noNameAndCodeForAirline)

        airline.mAirlineName = "Jetairfly"
        airline.mAirlineCode = null
        val nameButNoCodeForAirline = FormatUtils.formatAirline(airline, getContext())
        assertEquals(airline.mAirlineName, nameButNoCodeForAirline)

        airline.mAirlineName = null
        airline.mAirlineCode = "TB"
        val codeButNoNameForAirline = FormatUtils.formatAirline(airline, getContext())
        assertEquals(airline.mAirlineCode, codeButNoNameForAirline)

        airline.mAirlineName = "Jetairfly"
        airline.mAirlineCode = "TB"
        val codeAndNameForAirline = FormatUtils.formatAirline(airline, getContext())
        assertEquals(airline.mAirlineName + " (" + airline.mAirlineCode + ")", codeAndNameForAirline)
    }

    @Test
    fun formatDistanceTest() {
        val miles = 20
        var value = FormatUtils.formatDistance(getContext(), miles, FormatUtils.F_METRIC)
        assertEquals("32 km", value)

        value = FormatUtils.formatDistance(getContext(), miles, FormatUtils.F_LONG)
        assertEquals("20 miles", value)
    }

    @Test
    fun formatTimeZoneTest() {
        var value = FormatUtils.formatTimeZone(null, DateTime(), 6)
        assertEquals("", value)

        val airport = Airport()
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("", value)

        airport.mTimeZone = DateTimeZone.forOffsetHours(0)
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("GMT", value)

        airport.mTimeZone = DateTimeZone.forOffsetHours(-1)
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("GMT-1", value)

        airport.mTimeZone = DateTimeZone.forOffsetHours(5)
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("GMT+5", value)

        airport.mTimeZone = DateTimeZone.forOffsetHours(-10)
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("GMT10", value)

        airport.mTimeZone = DateTimeZone.forOffsetHours(13)
        value = FormatUtils.formatTimeZone(airport, DateTime(), 6)
        assertEquals("GMT13", value)

        airport.mTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Calcutta"))
        value = FormatUtils.formatTimeZone(airport, DateTime(), 0)
        assertEquals("IST", value)

        airport.mTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Calcutta"))
        value = FormatUtils.formatTimeZone(airport, DateTime(), 2)
        assertEquals("GMT+5:30", value)

        airport.mTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
        value = FormatUtils.formatTimeZone(airport, DateTime(), 2)
        assertEquals("GMT+3", value)
    }

    @Test
    fun parseFlightStatsDateTimeTest() {
        val actualLocalDateTime = FlightStatsDbUtils.parseFlightStatsDateTime("2017-11-28-12:23")
        val expectedLocalDateTime = LocalDateTime(2017, 11, 28, 12, 23)
        assertEquals(actualLocalDateTime, expectedLocalDateTime)
    }
}
