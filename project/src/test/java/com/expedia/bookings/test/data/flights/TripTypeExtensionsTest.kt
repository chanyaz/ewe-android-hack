package com.expedia.bookings.test.data.flights

import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchParams.TripType
import com.expedia.bookings.data.flights.extensions.isMultiDest
import com.expedia.bookings.data.flights.extensions.isOneWay
import com.expedia.bookings.data.flights.extensions.isRoundTrip
import com.expedia.bookings.test.MockFlightServiceTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class TripTypeExtensionsTest {
    var serviceRule = MockFlightServiceTestRule()
        @Rule get
    lateinit var builder: FlightSearchParams.Builder
    lateinit var tripType: TripType

    @Before
    fun setUp() {
        builder = serviceRule.flightSearchParamsBuilder(true)
    }

    @Test
    fun testTripTypeRoundTrip() {
        tripType = builder.tripType(FlightSearchParams.TripType.RETURN).build().tripType!!
        assertEquals(true, tripType.isRoundTrip())

        tripType = builder.tripType(FlightSearchParams.TripType.ONE_WAY).build().tripType!!
        assertEquals(false, tripType.isRoundTrip())
    }

    @Test
    fun testTripTypeOneWay() {
        tripType = builder.tripType(FlightSearchParams.TripType.ONE_WAY).build().tripType!!
        assertEquals(true, tripType.isOneWay())

        tripType = builder.tripType(FlightSearchParams.TripType.RETURN).build().tripType!!
        assertEquals(false, tripType.isOneWay())
    }

    @Test
    fun testTripTypeMultiDest() {
        tripType = builder.tripType(FlightSearchParams.TripType.MULTI_DEST).build().tripType!!
        assertEquals(true, tripType.isMultiDest())

        tripType = builder.tripType(FlightSearchParams.TripType.ONE_WAY).build().tripType!!
        assertEquals(false, tripType.isMultiDest())
    }
}
