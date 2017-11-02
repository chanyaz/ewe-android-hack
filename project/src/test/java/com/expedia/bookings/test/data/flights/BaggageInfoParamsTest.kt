package com.expedia.bookings.test.data.flights

import com.expedia.bookings.data.flights.BaggageInfoParams
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaggageInfoParamsTest {

    @Test
    fun testSingleFlightSegment() {
        val flightLeg = FlightLeg()
        val baggageInfoParams = BaggageInfoParams()
        var expectedResult = ArrayList<HashMap<String, String>>()
        expectedResult.add(getExpectedHashMap(("1")))
        val flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightSegments.add(getFlightSegment())
        flightLeg.flightSegments = flightSegments
        val receivedResult = baggageInfoParams.makeBaggageParams(flightLeg)
        assertEquals(expectedResult, receivedResult)
    }

    @Test
    fun testMultipleFlightSegment() {
        val flightLeg = FlightLeg()
        val baggageInfoParams = BaggageInfoParams()
        var expectedResult = ArrayList<HashMap<String, String>>()
        expectedResult.add(getExpectedHashMap("1"))
        expectedResult.add(getExpectedHashMap("2"))
        val flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightSegments.add(getFlightSegment())
        flightSegments.add(getFlightSegment())
        flightLeg.flightSegments = flightSegments
        val receivedResult = baggageInfoParams.makeBaggageParams(flightLeg)
        assertEquals(expectedResult, receivedResult)
    }

    private fun getFlightSegment(): FlightLeg.FlightSegment {
        val flightSegment = FlightLeg.FlightSegment()
        flightSegment.arrivalAirportCode = "LHR"
        flightSegment.departureAirportCode = "DXB"
        flightSegment.seatClass = "coach"
        flightSegment.airlineCode = "EK"
        flightSegment.operatingAirlineCode = ""
        flightSegment.bookingCode = "U"
        flightSegment.departureTime = "October 23, 2017 03:03:03 AM"
        flightSegment.flightNumber = "4"
        return flightSegment
    }

    private fun getExpectedHashMap(segmentNumber: String): HashMap<String, String> {
        val expectedHashMapSegment: HashMap<String, String> = (hashMapOf(
                "originapt" to "LHR",
                "destinationapt" to "DXB",
                "cabinclass" to "3",
                "mktgcarrier" to "EK",
                "opcarrier" to "",
                "bookingclass" to "U",
                "traveldate" to "10/23/2017",
                "flightnumber" to "4"
        ))
        expectedHashMapSegment.put("segmentnumber", segmentNumber)
        return expectedHashMapSegment
    }
}